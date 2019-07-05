package dk.repl

import com.intellij.openapi.Disposable

class DisposableMock : Disposable {
    @Volatile
    var isDisposed: Boolean = false

    override fun dispose() {
        isDisposed = true
    }
}