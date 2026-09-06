plugins {
    id("dev.kikugie.stonecutter")
}

// The version you're actively editing. Switch with the "Set active project to ..."
// Gradle tasks. See MIGRATION_NOTES.md for the single-target and matrix build commands.
stonecutter active "1.21.4"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\""
    swaps["minecraft"] = "\"${node.metadata.version}\""
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
}
