/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

interface DelegatedContainer: DelegatedComponent {
    /** The delegate to which all operations are delegated. */
    override val delegate: ContainerDelegate
}
