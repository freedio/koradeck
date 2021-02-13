/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model

import com.coradec.coradeck.conf.model.impl.MappedConfiguration

internal data class ConfigurationsConfig(val configurationReaders: Map<String, Class<*>>)
