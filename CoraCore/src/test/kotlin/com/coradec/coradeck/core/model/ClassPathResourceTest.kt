package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.trouble.ClassPathResourceNotFoundException
import com.coradec.coradeck.core.util.CURRENT_DIR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStream
import java.net.URL

internal class ClassPathResourceTest {

    @Test fun testExisting() {
        // given:
        val testee = ClassPathResource(this::class, "testExisting.txt")
        // when:
        val r1 = testee.exists
        val r2 = testee.location
        val r3 = testee.content
        val r4 = testee.file
        val r5 = testee.lines
        val r6 = testee.path
        val r7 = testee.stream
        // then:
        assertThat(r1).isTrue()
        assertThat(r2).isEqualTo(URL("file:$CURRENT_DIR/projects/coradec/coradeck/CoraCore/target/test-classes/com/coradec/coradeck/core/model/ClassPathResourceTest/testExisting.txt"))
        assertThat(r3).isEqualTo("This is a test resource.\nIt has two lines.")
        assertThat(r4).isEqualTo(File("$CURRENT_DIR/projects/coradec/coradeck/CoraCore/target/test-classes/com/coradec/coradeck/core/model/ClassPathResourceTest/testExisting.txt"))
        assertThat(r5).containsExactly("This is a test resource.", "It has two lines.")
        assertThat(r6).isEqualTo("com/coradec/coradeck/core/model/ClassPathResourceTest/testExisting.txt")
        assertThat(r7).isInstanceOf(InputStream::class.java)
    }

    @Test fun testNonExisting() {
        // given:
        val testee = ClassPathResource(this::class, "testNonExisting.txt")
        // when:
        val r1 = testee.exists
        val r2 = try { testee.location } catch (e: ClassPathResourceNotFoundException) { null }
        val r3 = try { testee.content } catch (e: ClassPathResourceNotFoundException) { null }
        val r4 = try { testee.file } catch (e: ClassPathResourceNotFoundException) { null }
        val r5 = try { testee.lines } catch (e: ClassPathResourceNotFoundException) { null }
        val r6 = testee.path
        val r7 = try { testee.stream } catch (e: ClassPathResourceNotFoundException) { null }
        // then:
        assertThat(r1).isFalse()
        assertThat(r2).isNull()
        assertThat(r3).isNull()
        assertThat(r4).isNull()
        assertThat(r5).isNull()
        assertThat(r6).isEqualTo("com/coradec/coradeck/core/model/ClassPathResourceTest/testNonExisting.txt")
        assertThat(r7).isNull()
    }
}