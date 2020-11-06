package com.ahao.videocacheserver.cache;

import java.io.File;
import java.util.List;

public class CommonListFile implements ListFile {
    private List<File> files;

    public CommonListFile(List<File> files) {
        this.files = files;
    }

    @Override
    public synchronized File consume() {
        if (files.isEmpty()) {
            return null;
        }
        return files.remove(0);
    }

    @Override
    public void server(File file) {

    }
}
