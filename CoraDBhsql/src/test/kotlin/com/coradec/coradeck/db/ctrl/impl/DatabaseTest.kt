/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.ctrl.impl

import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.bus.module.CoraBusImpl
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.Files
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.core.util.toPath
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.db.com.OpenTableVoucher
import com.coradec.coradeck.db.model.impl.HsqlDatabase
import com.coradec.coradeck.db.module.CoraDbHsql
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.model.Password
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import java.net.URI
import java.time.LocalDate

@Disabled
class DatabaseTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(
                CoraConfImpl::class,
                CoraComImpl::class,
                CoraTextImpl::class,
                CoraTypeImpl::class,
                CoraDirImpl::class,
                CoraControlImpl::class,
                CoraBusImpl::class,
                CoraDbHsql::class
            )
            val log = CoraCom.log
            Files.deleteTree("/tmp/dbtest3".toPath())
            Files.deleteTree("/tmp/dbtest4".toPath())
            HsqlDbTest.database1 = HsqlDatabase(URI("jdbc:hsqldb:file:/tmp/dbtest3/db"), "sa", Password(""))
            CoraBus.applicationBus.add("hsqlDB1", HsqlDbTest.database1.memberView).standby()
            HsqlDbTest.database1.accept(OpenTableVoucher(here, HsqlDbTest.TestClass::class)).standby()
            HsqlDbTest.database1.accept(OpenTableVoucher(here, HsqlDbTest.TestClass2::class)).content.value.let { table ->
                Thread.sleep(500)
                table += HsqlDbTest.TestClass2("Jane", "Doe", LocalDate.of(2000, 1, 1), 2)
                table += HsqlDbTest.TestClass2("Jack", "Daniels", LocalDate.of(1864, 4, 24), 1)
            }
            HsqlDbTest.database1.accept(OpenTableVoucher(here, HsqlDbTest.TestClassWithCurrency::class)).standby()
            log.debug("Test suite initialized.")
            relax()
            HsqlDbTest.database2 = HsqlDatabase(URI("jdbc:hsqldb:file:/tmp/dbtest4/db"), "sa", Password(""))
            CoraBus.applicationBus.add("hsqlDB2", HsqlDbTest.database2.memberView).standby()
            HsqlDbTest.database2.accept(OpenTableVoucher(here, HsqlDbTest.TestClass::class)).standby()
            HsqlDbTest.database2.accept(OpenTableVoucher(here, HsqlDbTest.TestClass2::class)).content.value.let { table ->
                Thread.sleep(500)
                table += HsqlDbTest.TestClass2("Jane", "Doe", LocalDate.of(2000, 1, 1), 2)
                table += HsqlDbTest.TestClass2("Jack", "Daniels", LocalDate.of(1864, 4, 24), 1)
            }
            HsqlDbTest.db1 = HsqlDbTest.database1.databaseView
            HsqlDbTest.db2 = HsqlDbTest.database2.databaseView
        }

        @AfterAll
        @JvmStatic fun tearDown() {
            val log = CoraCom.log
            log.debug("Tear down.")
            HsqlDbTest.database1.detach().standby()
            HsqlDbTest.database2.detach().standby()
            Thread.sleep(1000)
            log.debug("Torn down.")
        }
    }

}
