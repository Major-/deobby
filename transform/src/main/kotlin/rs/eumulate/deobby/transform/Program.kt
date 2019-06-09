package rs.eumulate.deobby.transform

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter
import rs.eumulate.deobby.asm.SupertypeAwareClassWriter
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

    private val classes = classes.toMutableList()

    fun write(path: Path) {
        val supertypes = classes.associateBy(ClassNode::name, ClassNode::superName)

        if (classes.size == 1) {
            val clazz = classes.first()
            val outputPath = path.resolveSibling("${clazz.name}.deob.class")

            Files.newOutputStream(outputPath, CREATE, TRUNCATE_EXISTING).use { out ->
                val bytes = clazz.encode(supertypes)
                out.write(bytes)
            }
        } else {
            JarOutputStream(Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING)).use { out ->
                for (clazz in classes) {
                    out.putNextEntry(JarEntry("${clazz.name}.class"))
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
        val writer = SupertypeAwareClassWriter(ClassWriter.COMPUTE_MAXS, supertypes)

        try {
            accept(CheckClassAdapter(writer, true))
        } catch (e: Exception) {
            throw Exception("Error encoding $name.class", e)
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
                    .filter { it.name.endsWith(".class") }
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