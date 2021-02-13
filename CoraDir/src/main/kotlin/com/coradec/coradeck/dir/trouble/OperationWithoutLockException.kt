package com.coradec.coradeck.dir.trouble

import com.coradec.coradeck.dir.model.Path

class OperationWithoutLockException(operation: String, requiredLock: String, val path: Path) :
        DirectoryLockingException("Operation ‹$operation› requires a $requiredLock lock!")
