package com.coradec.coradeck.dir.trouble

import com.coradec.coradeck.dir.model.Path

class DirectoryEntryAlreadyExistsException(val path: Path) : DirectoryException() {

}
