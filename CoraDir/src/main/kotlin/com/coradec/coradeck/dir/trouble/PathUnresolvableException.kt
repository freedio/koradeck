package com.coradec.coradeck.dir.trouble

import com.coradec.coradeck.dir.model.Path

class PathUnresolvableException(val path: Path) : DirectoryException()
