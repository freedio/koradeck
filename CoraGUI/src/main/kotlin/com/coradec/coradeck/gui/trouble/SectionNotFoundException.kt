/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.trouble

import com.coradec.coradeck.gui.model.SectionIndex

class SectionNotFoundException(val section: SectionIndex) : BasicGUIException() {

}
