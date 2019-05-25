package rs.eumulate.deobby.transform

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter
import java.io.BufferedInputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.stream.Collectors

class Program private constructor(
    val classes: MutableSet<ClassNode>
) {

    constructor(classes: Collection<ClassNode>) : this(classes.toMutableSet())

    fun writeJar(path: Path) {
        JarOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE)).use { out ->
            for (clazz in classes) {
                val writer = ClassWriter(0)

                clazz.accept(CheckClassAdapter(writer, true))

                out.putNextEntry(JarEntry("${clazz.name}.class"))
                out.write(writer.toByteArray())
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

            return Program(classes)
        }

        private fun readClass(path: Path): Set<ClassNode> {
            val input = BufferedInputStream(Files.newInputStream(path))
            val node = ClassNode().also {
                ClassReader(input).accept(it, CLASS_PARSING_OPTIONS)
            }

            return setOf(node)
        }

        private fun readZip(path: Path): Set<ClassNode> {
            return JarFile(path.toFile()).use { file ->
                file.stream()
                    .filter { it.name.endsWith(".class") }
                    .map { entry ->
                        file.getInputStream(entry).use { input ->
                            ClassNode().also {
                                ClassReader(input).accept(it, CLASS_PARSING_OPTIONS)
                            }
                        }
                    }
                    .collect(Collectors.toSet())
            }
        }

    }

}