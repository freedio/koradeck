/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.trouble

class NoBusApplicationOnThreadException(val thread: Thread = Thread.currentThread()) : BusException() {
}
