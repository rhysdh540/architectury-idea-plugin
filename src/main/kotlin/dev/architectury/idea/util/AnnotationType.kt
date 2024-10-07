package dev.architectury.idea.util

enum class AnnotationType(private val annotations: Set<String>) : Set<String> by annotations {
    EXPECT_PLATFORM(
        "dev.architectury.injectables.annotations.ExpectPlatform",
        "me.shedaniel.architectury.annotations.ExpectPlatform",
        "me.shedaniel.architectury.ExpectPlatform",
        "xyz.wagyourtail.unimined.expect.annotation.ExpectPlatform"
    ),
    TRANSFORMED_EXPECT_PLATFORM(
        "dev.architectury.injectables.annotations.ExpectPlatform.Transformed",
        "me.shedaniel.architectury.annotations.ExpectPlatform.Transformed",
        "xyz.wagyourtail.unimined.expect.annotation.ExpectPlatform.Transformed"
    ),
    PLATFORM_ONLY(
        "dev.architectury.injectables.annotations.PlatformOnly",
        "me.shedaniel.architectury.annotations.PlatformOnly",
        "xyz.wagyourtail.unimined.expect.annotation.PlatformOnly"
    )
    ;

    constructor(vararg annotations: String) : this(annotations.toSet())
}
