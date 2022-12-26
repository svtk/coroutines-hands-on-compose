package contributors

import variant.contributors.Variant
import java.util.prefs.Preferences

private fun prefNode(): Preferences = Preferences.userRoot().node("ContributorsUI")

data class LoadingParams(val username: String, val password: String, val org: String, val variant: Variant)

fun loadStoredParams(): LoadingParams {
    return prefNode().run {
        LoadingParams(
            get("username", ""),
            get("password", ""),
            get("org", "kotlin"),
            Variant.valueOf(get("variant", Variant.BLOCKING.name))
        )
    }
}

fun removeStoredParams() {
    prefNode().removeNode()
}

fun saveParams(loadingParams: LoadingParams) {
    prefNode().apply {
        put("username", loadingParams.username)
        put("password", loadingParams.password)
        put("org", loadingParams.org)
        put("variant", loadingParams.variant.name)
        sync()
    }
}