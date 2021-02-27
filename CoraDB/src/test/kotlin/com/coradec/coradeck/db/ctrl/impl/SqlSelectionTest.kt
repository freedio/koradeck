package com.coradec.coradeck.db.ctrl.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SqlSelectionTest {

    @Test
    fun testOffset() {
        // given
        val testee1 = SqlSelection("whatever offset:12 etc.")
        val testee2 = SqlSelection("whatever OFFSET:13")
        val testee3 = SqlSelection("Offset:14 etc.")
        val testee4 = SqlSelection("OFFSET:15")
        val testee5 = SqlSelection("offset:-1 etc.")
        val testee6 = SqlSelection("whatever Offset:0 etc.")
        // when:
        val offset1 = testee1.offset
        val offset2 = testee2.offset
        val offset3 = testee3.offset
        val offset4 = testee4.offset
        val offset5 = testee5.offset
        val offset6 = testee6.offset
        // then:
        assertThat(offset1).isEqualTo(" offset 12")
        assertThat(offset2).isEqualTo(" offset 13")
        assertThat(offset3).isEqualTo(" offset 14")
        assertThat(offset4).isEqualTo(" offset 15")
        assertThat(offset5).isEqualTo("")
        assertThat(offset6).isEqualTo("")
    }

    @Test
    fun testLimit() {
        // given
        val testee1 = SqlSelection("whatever limit:12 etc.")
        val testee2 = SqlSelection("whatever LIMIT:13")
        val testee3 = SqlSelection("Limit:14 etc.")
        val testee4 = SqlSelection("limit:15")
        val testee5 = SqlSelection("LIMIT:-1 etc.")
        val testee6 = SqlSelection("whatever Limit:0 etc.")
        // when:
        val limit1 = testee1.limit
        val limit2 = testee2.limit
        val limit3 = testee3.limit
        val limit4 = testee4.limit
        val limit5 = testee5.limit
        val limit6 = testee6.limit
        // then:
        assertThat(limit1).isEqualTo(" limit 12")
        assertThat(limit2).isEqualTo(" limit 13")
        assertThat(limit3).isEqualTo(" limit 14")
        assertThat(limit4).isEqualTo(" limit 15")
        assertThat(limit5).isEqualTo("")
        assertThat(limit6).isEqualTo("")
    }

    @Test fun testSlice() {
        // given
        val testee1 = SqlSelection("whatever limit:12 etc.")
        val testee2 = SqlSelection("whatever OFFSET:13")
        val testee3 = SqlSelection("Limit:14 etc. Offset:1")
        val testee4 = SqlSelection("Offset:1limit:15")
        val testee5 = SqlSelection("Offset:100:LIMIT:-1 etc.")
        val testee6 = SqlSelection("whatever Limit:0, Offset:0 etc.")
        // when:
        val r1 = testee1.slice
        val r2 = testee2.slice
        val r3 = testee3.slice
        val r4 = testee4.slice
        val r5 = testee5.slice
        val r6 = testee6.slice
        // then:
        assertThat(r1).isEqualTo(" limit 12")
        assertThat(r2).isEqualTo(" offset 13")
        assertThat(r3).isEqualTo(" offset 1 limit 14")
        assertThat(r4).isEqualTo(" offset 1 limit 15")
        assertThat(r5).isEqualTo(" offset 100")
        assertThat(r6).isEqualTo("")
    }

    @Test fun testWhere() {
        // given
        val t1 = SqlSelection("&%^$[f1=3][f2= 'hello'] and [f3 = \"sym\"]")
        val t2 = SqlSelection("[f1=3][f2= 'hello'] or [f3 = \"sym\"]&%^\$")
        val t3 = SqlSelection("&%^$[f1<=3][f2 Like 'hello'] or [f3 NOT BETWEEN \"sym\" And \"zoom\"]&%^\$")
        val t4 = SqlSelection("&%^$[f1<> 1][f2  Not In ('hello', 'yallo')] or [f3    >   21]&%^\$")
        val t5 = SqlSelection("[f1  exists][f2 Not   In ('hello', 'yallo')] or [f3   IS NOT  NULL]")
        // when:
        val r1 = t1.where
        val r2 = t2.where
        val r3 = t3.where
        val r4 = t4.where
        val r5 = t5.where
        // then:
        assertThat(r1).isEqualTo(" where f1 = 3 and f2 = 'hello' and f3 = \"sym\"")
        assertThat(r2).isEqualTo(" where f1 = 3 and f2 = 'hello' and f3 = \"sym\"")
        assertThat(r3).isEqualTo(" where f1 <= 3 and f2 like 'hello' and f3 not between \"sym\" And \"zoom\"")
        assertThat(r4).isEqualTo(" where f1 <> 1 and f2 not in ('hello', 'yallo') and f3 > 21")
        assertThat(r5).isEqualTo(" where f2 not in ('hello', 'yallo') and f1 exists and f3 is not null")
    }

    @Test fun testOrder() {
        // given
        val t1 = SqlSelection("whatever f1:asc f2:desc f3:ASC f4:DESC f5:Asc f6:Desc a.s.o.")
        val t2 = SqlSelection("whatever limit:asc, offset:descf1:ASCf2:DESC")
        // when:
        val r1 = t1.order
        val r2 = t2.order
        // then:
        assertThat(r1).isEqualTo(" order by f1 asc, f2 desc, f3 asc, f4 desc, f5 asc, f6 desc")
        assertThat(r2).isEqualTo(" order by limit asc, offset desc, f1 asc, f2 desc")
    }

    @Test fun testFilter() {
        // given
        val t1 = SqlSelection("here we go: limit:3 limit:asc [limit<24]")
        val t2 = SqlSelection("here we go: limit:asc [limit<24] limit:3")
        val t3 = SqlSelection("here we go: offset:desc [offset NOT LIKE 'picasso'] [limit Is not Null] offset:3")
        // when:
        val r1 = t1.filter
        val r2 = t2.filter
        val r3 = t3.filter
        // then:
        assertThat(r1).isEqualTo(" limit 3 where limit < 24")
        assertThat(r2).isEqualTo(" limit 3 where limit < 24")
        assertThat(r3).isEqualTo(" offset 3 where offset not like 'picasso' and limit is not null")
    }

    @Test fun testSelect() {
        // given
        val t1 = SqlSelection("here we go: limit:3 limit:asc [limit<24]")
        val t2 = SqlSelection("here we go: limit:ASC [limit<24] limit:3 and some more")
        val t3 = SqlSelection("here we go: offset:Desc [offset NOT LIKE 'picasso'] [limit Is not Null] offset:3")
        // when:
        val r1 = t1.select
        val r2 = t2.select
        val r3 = t3.select
        // then:
        assertThat(r1).isEqualTo(" limit 3 where limit < 24 order by limit asc")
        assertThat(r2).isEqualTo(" limit 3 where limit < 24 order by limit asc")
        assertThat(r3).isEqualTo(" offset 3 where offset not like 'picasso' and limit is not null order by offset desc")
    }
}