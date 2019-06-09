package rs.eumulate.deobby.asm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SupertypeAwareClassWriterTest {

    @Test
    fun `two loaded classes that extend Object returns Object`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass("java/lang/Class", "java/lang/Throwable")

        assertEquals("java/lang/Object", type)
    }

    @Test
    fun `two loaded classes with a common non-Object supertype returns the common supertype`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass("java/lang/Exception", "java/lang/RuntimeException")

        assertEquals("java/lang/Exception", type)
    }

    @Test
    fun `two loaded classes with common non-Object supertypes returns the closest common supertype`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass(
            "java/lang/NegativeArraySizeException",
            "java/lang/EnumConstantNotPresentException"
        )

        assertEquals("java/lang/RuntimeException", type)
    }

    @Test
    fun `loaded class that implements the given loaded interface returns the given interface`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass("java/util/ArrayList", "java/lang/Iterable")

        assertEquals("java/lang/Iterable", type)
    }

    @Test
    fun `loaded class that does not implement the given loaded interface returns Object`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass("java/util/ArrayList", "java/util/Set")

        assertEquals("java/lang/Object", type)
    }

    @Test
    fun `Object type returns Object`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())

        val withInterface = writer.getCommonSuperClass("java/lang/Object", "java/util/Set")
        assertEquals("java/lang/Object", withInterface)

        val withSelf = writer.getCommonSuperClass("java/lang/Object", "java/lang/Object")
        assertEquals("java/lang/Object", withSelf)
    }

    @Test
    fun `two loaded interfaces returns Object`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass("java/lang/Comparable", "java/lang/Runnable")

        assertEquals("java/lang/Object", type)
    }

    @Test
    fun `two loaded interfaces with a common supertype returns Object`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass("java/util/List", "java/util/Set")

        assertEquals("java/lang/Object", type)
    }

    @Test
    fun `loaded interface that implements the other loaded interface returns the superinterface`() {
        val writer = SupertypeAwareClassWriter(0, emptyMap())
        val type = writer.getCommonSuperClass("java/util/List", "java/util/Collection")

        assertEquals("java/util/Collection", type)
    }

    @Test
    fun `one unloaded type and one loaded type with no common supertype returns Object`() {
        val writer = SupertypeAwareClassWriter(0, mapOf("hard/days/Night" to "java/lang/Object"))
        val type = writer.getCommonSuperClass("hard/days/Night", "java/util/ArrayList")

        assertEquals("java/lang/Object", type)
    }

    @Test
    fun `one unloaded type and one loaded type with a common supertype returns the supertype`() {
        val writer = SupertypeAwareClassWriter(0, mapOf("hard/days/Night" to "java/lang/RuntimeException"))
        val type = writer.getCommonSuperClass("hard/days/Night", "java/lang/ArrayIndexOutOfBoundsException")

        assertEquals("java/lang/RuntimeException", type)
    }

    @Test
    fun `two unloaded types with a common loaded supertype returns the supertype`() {
        val classes = mapOf(
            "hard/days/Night" to "java/util/ArrayList",
            "beatles/for/Sale" to "java/util/ArrayList"
        )

        val writer = SupertypeAwareClassWriter(0, classes)
        val type = writer.getCommonSuperClass("hard/days/Night", "beatles/for/Sale")

        assertEquals("java/util/ArrayList", type)
    }

    @Test
    fun `two unloaded types with the same unloaded supertype returns the supertype`() {
        val classes = mapOf(
            "hard/days/Night" to "let/it/Be",
            "beatles/for/Sale" to "let/it/Be",
            "let/it/Be" to "java/lang/Object"
        )

        val writer = SupertypeAwareClassWriter(0, classes)
        val type = writer.getCommonSuperClass("hard/days/Night", "beatles/for/Sale")

        assertEquals("let/it/Be", type)
    }

    @Test
    fun `two unloaded types with different unloaded parents with a common supertype returns the supertype`() {
        val classes = mapOf(
            "hard/days/Night" to "let/it/Be",
            "beatles/for/Sale" to "please/please/Me",
            "please/please/Me" to "java/util/ArrayList",
            "let/it/Be" to "java/util/ArrayList"
        )

        val writer = SupertypeAwareClassWriter(0, classes)
        val type = writer.getCommonSuperClass("hard/days/Night", "beatles/for/Sale")

        assertEquals("java/util/ArrayList", type)
    }

    @Test
    fun `writer can have types added after construction`() {
        val writer = SupertypeAwareClassWriter(0, mapOf("hard/days/Night" to "java/lang/Object"))
        writer["beatles/for/Sale"] = "java/lang/Object"

        val type = writer.getCommonSuperClass("hard/days/Night", "java/util/ArrayList")
        assertEquals("java/lang/Object", type)
    }

}