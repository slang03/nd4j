package org.nd4j.linalg.util;

import lombok.NonNull;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author raver119@gmail.com
 */
public class FileUtils {

    public static Collection<File> listFiles(@NonNull File rootFolder) throws IOException {
        val list = new ArrayList<File>();

        if (!rootFolder.exists())
            return list;

        val c = rootFolder.listFiles();
        for (val c2: c) {
            if (c2.isDirectory())
                list.addAll(listFiles(c2));
            else
                list.add(c2);
        }

        return list;
    }
}
