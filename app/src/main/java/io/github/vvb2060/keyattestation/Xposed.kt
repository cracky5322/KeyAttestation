package io.github.vvb2060.keyattestation

import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.security.KeyStoreException
import java.security.ProviderException
import java.util.*

class Xposed : IXposedHookZygoteInit {
    override fun initZygote(startupParam: StartupParam) {

        val hook: XC_MethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                XposedBridge.log("New key attestation request: ${Arrays.toString(param.args)}")
                param.throwable = ProviderException("Failed to generate attestation certificate chain",
                        KeyStoreException((-10003).toString()))
            }
        }

        XposedHelpers.findClassIfExists(
                "android.security.keystore.AndroidKeyStoreKeyPairGeneratorSpi", null)
                ?.also { XposedBridge.log("Found KeyStore: $it") }
                ?.declaredMethods?.find { it.name == "getAttestationChain" }
                ?.let { XposedBridge.hookMethod(it, hook) }
                ?.apply { XposedBridge.log("KeyStore method hooked: ${hookedMethod}") }
    }
}
