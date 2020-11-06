package com.ahao.videocacheserver.cache;

import java.io.File;

public interface ListFile {
    File consume();

    void server(File file);
}
