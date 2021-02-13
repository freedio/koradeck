/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SessionUT {

    @Test fun newSession() {
        // given
        val currentUser = System.getProperty("user.name")!!
        // when
        val result = Session.new
        // then
        assertThat(result.user).isEqualTo(currentUser)
        assertThat(result.authenticated).isTrue()
    }

    @Test fun newSecureSession() {
        // given
        val currentUser = System.getProperty("user.name")!!
        // when
        val result = Session.secure
        // then
        assertThat(result.user).isEqualTo(currentUser)
        assertThat(result.authenticated).isFalse()
    }

    @Test fun newExplicitSession() {
        // given
        val currentUser = System.getProperty("user.name")!!
        // when
        val result = Session.new("boney")
        // then
        assertThat(result.user).isNotEqualTo(currentUser)
        assertThat(result.authenticated).isFalse()
    }

    @Test fun newExplicitSecureSession() {
        // given
        val currentUser = System.getProperty("user.name")!!
        // when
        val result = Session.secure("boney")
        // then
        assertThat(result.user).isNotEqualTo(currentUser)
        assertThat(result.authenticated).isFalse()
    }

}
