/*
 * Copyright 2019 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.babylon.certificatetransparency.internal.loglist.model.v2

import com.babylon.certificatetransparency.utils.TestData
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogListV2Test {

    @Test
    fun verifyGsonParser() {
        val json = TestData.file(TestData.TEST_LOG_LIST_JSON).readText()

        val logList = GsonBuilder().setLenient().create().fromJson(json, LogListV2::class.java)

        val google = logList.operators.first { it.name == "Google" }
        val cloudflare = logList.operators.first { it.name == "Cloudflare" }
        val certly = logList.operators.first { it.name == "Certly" }

        assertEquals(12, google.logs.size)
        assertEquals(5, cloudflare.logs.size)
        assertEquals(1, certly.logs.size)

        val argon2021 = google.logs.first { it.description == "Google 'Argon2021' log" }
        assertEquals(
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAETeBmZOrzZKo4xYktx9gI2chEce3cw/tbr5xkoQlmhB18aKfsxD+MnILgGNl0FOm0eYGilFVi85wLRIOhK8lxKw==",
            argon2021.key
        )
        assertTrue(argon2021.state is State.Usable)

        val aviatorLog = google.logs.first { it.description == "Google 'Aviator' log" }
        assertTrue(aviatorLog.state is State.ReadOnly)
        assertEquals(46466472, (aviatorLog.state as State.ReadOnly).finalTreeHead.treeSize)

        val nimbusLog = cloudflare.logs.first { it.description == "Cloudflare 'Nimbus2022' Log" }
        assertEquals(86400, nimbusLog.maximumMergeDelay)
        assertEquals(1559606400000, nimbusLog.state?.timestamp)
    }
}
