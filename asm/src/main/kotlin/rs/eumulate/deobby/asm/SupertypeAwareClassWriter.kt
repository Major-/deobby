package rs.eumulate.deobby.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

/**
 * A [ClassWriter] that allows users to manually specify the supertypes of classes that are to be written using a
 * [ClassWriter], but aren't available on the classpath.
 */
class SupertypeAwareClassWriter(
    classReader: ClassReader?,
    flags: Int,
    private val classes: MutableMap<String, String>
) : ClassWriter(classReader, flags), MutableMap<String, String> by classes {

    constructor(flags: Int, classes: Map<String, String>) : this(null, flags, classes.toMutableMap())

    /**
     * Adds a mapping between the class named [key] and its supertype name [value].
     */
    override fun put(key: String, value: String): String? {
        require(value in this || classLoader.canLoadClass(value)) {
            "Tried to add a a type $key with an unrecognised supertype $value."
        }

        return classes.put(key, value)
    }

    public override fun getCommonSuperClass(type1: String, type2: String): String {
        if (type1 !in this && type2 !in this) {
            return super.getCommonSuperClass(type1, type2)
        }

        val supers1 = getSuperClasses(type1)
        val supers2 = getSuperClasses(type2, set = true)

        return supers1.firstOrNull(supers2::contains) ?: "java/lang/Object"
    }

    private fun ClassLoader.canLoadClass(value: String): Boolean {
        // TODO can this be done better?
        return try {
            loadClass(value.replace('/', '.'))
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Collects all of the superclass names for the specified class [type].
     * @param set Whether or not the returned collection should be a [MutableSet].
     */
    private fun getSuperClasses(type: String, set: Boolean = false): MutableCollection<String> {
        val supers: MutableCollection<String> = if (set) HashSet() else ArrayList()
        var supertype = type

        while (supertype in this) {
            supers += supertype
            supertype = get(supertype)!!
        }

        var superclass = Class.forName(supertype.replace('/', '.'))
        while (superclass != null) {
            supers += superclass.canonicalName.replace('.', '/')
            superclass = superclass.superclass
        }

        return supers
    }

}