package com.sergsave.purryourcat.content

import android.net.Uri

interface IContentStorage {
    fun store(sourceContent: Uri, fileName: String): Uri?
    fun store(sourceContent: Uri): Uri? // keep source file name
    fun read(): List<Uri>?
    fun remove(uri: Uri): Boolean
}