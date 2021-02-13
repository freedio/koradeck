package com.coradec.coradeck.dir.trouble

import com.coradec.coradeck.dir.model.Path

class DirectoryNotFoundException(val path: Path) : DirectoryException()
