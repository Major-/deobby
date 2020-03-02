package rs.emulate.deobby.transform

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.JSRInlinerAdapter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter
import rs.emulate.deobby.asm.SupertypeAwareClassWriter
import java.io.BufferedInputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.stream.Collectors
import java.util.zip.ZipEntry

class Program(classes: List<ClassNode>, val context: ProgramContext) {

    private val classes = classes.associateByTo(mutableMapOf(), ClassNode::name)

    init {
        // Before letting the user do anything with this program, replace any jsr/ret instructions that ASM won't handle
        // (only produced by very old java compilers).
        for (`class` in classes) {
            for ((index, method) in `class`.methods.withIndex()) {
                val signature = method.signature
                val exceptions = method.exceptions.toTypedArray()

                val adapter = JSRInlinerAdapter(method, method.access, method.name, method.desc, signature, exceptions)
                method.accept(adapter)

                `class`.methods[index] = adapter
            }
        }
    }

    fun classes(): Collection<ClassNode> {
        return classes.values
    }

    /**
     * Adds the specified [ClassNode] to this [Program].
     *
     * @throws IllegalArgumentException If this [Program] already contains a class with the same internal name.
     */
    fun add(clazz: ClassNode) {
        require(clazz.name !in classes) { "ClassNode named ${clazz.name} already exists." }
        classes[clazz.name] = clazz
    }

    /**
     * Removes the specified [ClassNode] from this [Program].
     *
     * This function should only be called _after_ removing all references to the [ClassNode] from other nodes.
     */
    fun replace(old: ClassNode, new: ClassNode) {
        remove(old)
        plusAssign(new)
    }

    /**
     * Removes the specified [ClassNode] from this [Program].
     *
     * This function should only be called _after_ removing all references to the [ClassNode] from other nodes.
     */
    fun remove(clazz: ClassNode): Boolean {
        return classes.remove(clazz.name) != null
    }

    operator fun contains(name: String): Boolean {
        return name in classes
    }

    operator fun get(name: String): ClassNode {
        return requireNotNull(classes[name]) { "Unrecognised class name `$name`." }
    }

    operator fun plusAssign(clazz: ClassNode): Unit = add(clazz)

    /**
     * Writes the contents of this [Program] to the specified [Path].
     */
    fun write(path: Path) {
        val supertypes = classes.values.associateBy(ClassNode::name, ClassNode::superName)

        if (classes.size == 1) {
            val clazz = classes.values.first()
            val outputPath = if (Files.exists(path) && !path.toString().endsWith(".deob.class")) {
                path.resolveSibling("${clazz.name}.deob.class")
            } else {
                path
            }

            Files.newOutputStream(outputPath, CREATE, TRUNCATE_EXISTING).use { out ->
                val bytes = clazz.encode(supertypes)
                out.write(bytes)
            }
        } else {
            JarOutputStream(Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING)).use { out ->
                for ((name, clazz) in classes) {
                    out.putNextEntry(JarEntry("${name}.class"))
                    out.write(clazz.encode(supertypes))
                }

                if (ZIP_MATCHER.matches(context.path)) {
                    JarFile(context.path.toFile(), /* verify = */ false).use { file ->
                        file.stream()
                            .filter { !it.name.endsWith(".class") }
                            .forEach { copyZipEntry(it, file, out) }
                    }
                }
            }
        }
    }

    private fun ClassNode.encode(supertypes: Map<String, String>): ByteArray {
        val writer = SupertypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES, supertypes)

        try {
            accept(CheckClassAdapter(writer, true))
        } catch (e: Exception) {
            throw Exception("Error encoding $name.class in ${context.path}", e)
        }

        return writer.toByteArray()
    }

    private fun copyZipEntry(entry: ZipEntry, jar: JarFile, out: JarOutputStream) {
        if (entry.isDirectory) {
            out.putNextEntry(JarEntry(entry.name))
        } else {
            jar.getInputStream(entry).use { input ->
                out.putNextEntry(JarEntry(entry.name))
                input.copyTo(out, minOf(entry.size.toInt(), 500 * 1024))
            }
        }
    }

    companion object {

        private var ZIP_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.{zip,jar}")

        private const val CLASS_PARSING_OPTIONS = ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES

        fun from(path: Path): Program {
            val classes = when {
                ZIP_MATCHER.matches(path) -> readZip(path)
                path.toString().endsWith(".class") -> readClass(path)
                else -> throw IllegalArgumentException("Unrecognised file type `$path`.")
            }

            return Program(classes, ProgramContext(path))
        }

        private fun readClass(path: Path): List<ClassNode> {
            val size = minOf(Files.size(path).toInt(), 500 * 1024)

            BufferedInputStream(Files.newInputStream(path), size).use { input ->
                val node = ClassNode().also {
                    ClassReader(input).accept(it, CLASS_PARSING_OPTIONS)
                }

                return listOf(node)
            }
        }

        private fun readZip(path: Path): List<ClassNode> {
            return JarFile(path.toFile(), /* verify = */ false).use { file ->
                file.stream()
                    .filter { it.name.endsWith(".class") } // TODO filter by 0xCAFEBABE header not file name?
                    .map { entry ->
                        file.getInputStream(entry).use { input ->
                            ClassNode().also {
                                ClassReader(input).accept(it, CLASS_PARSING_OPTIONS)
                            }
                        }
                    }
                    .collect(Collectors.toList())
                    .sortedBy(ClassNode::name)
            }
        }

    }

}