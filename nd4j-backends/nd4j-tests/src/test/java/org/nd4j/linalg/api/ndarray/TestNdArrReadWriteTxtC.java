
package org.nd4j.linalg.api.ndarray;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by susaneraly on 6/18/16.
 */
@Slf4j
@RunWith(Parameterized.class)
public class TestNdArrReadWriteTxtC extends BaseNd4jTest {

    public final static List<INDArray> ARRAYS_TO_TEST_CORDER = new ArrayList<INDArray>() {{
        //rank - 0
        add(Nd4j.trueScalar(4)); //[]
        // rank - 1
        add(Nd4j.rand('c', new int[]{1}));
        //rank - 2
        add(Nd4j.rand('c', new int[]{1, 1}));
        add(Nd4j.rand('c', new int[]{1, 2}));
        add(Nd4j.rand('c', new int[]{2, 1}));
        add(Nd4j.rand('c', new int[]{2, 2}));
        add(Nd4j.rand('c', new int[]{10, 10}));
        //rank - 3
        add(Nd4j.rand('c', new int[]{1, 1, 1}));
        add(Nd4j.rand('c', new int[]{1, 1, 2}));
        add(Nd4j.rand('c', new int[]{1, 2, 1}));
        add(Nd4j.rand('c', new int[]{2, 1, 1}));
        add(Nd4j.rand('c', new int[]{1, 2, 2}));
        add(Nd4j.rand('c', new int[]{2, 1, 2}));
        add(Nd4j.rand('c', new int[]{2, 2, 1}));
        add(Nd4j.rand('c', new int[]{2, 2, 2}));
        add(Nd4j.rand('c', new int[]{4, 2, 3}));
        //rank - 4
        add(Nd4j.rand('c', new int[]{2, 1, 1, 1}));
        add(Nd4j.rand('c', new int[]{1, 2, 1, 1}));
        add(Nd4j.rand('c', new int[]{1, 1, 2, 1}));
        add(Nd4j.rand('c', new int[]{1, 1, 1, 2}));
        add(Nd4j.rand('c', new int[]{1, 2, 1, 2}));
        add(Nd4j.rand('c', new int[]{1, 2, 2, 1}));
        add(Nd4j.rand('c', new int[]{2, 1, 1, 2}));
        add(Nd4j.rand('c', new int[]{2, 1, 2, 1}));
        add(Nd4j.rand('c', new int[]{2, 3, 2, 2}));
        //rank > 4
        add(Nd4j.rand('c', new int[]{3, 5, 3, 4, 6}));
    }};


    public TestNdArrReadWriteTxtC(Nd4jBackend backend) {

        super(backend);
    }

    @Test
    public void compareAfterWrite() {
        for (INDArray origArray : ARRAYS_TO_TEST_CORDER) {
            log.info("Checking shape ..." + ArrayUtils.toString(origArray.shape()));
            //log.info(origArray.toString());
            Nd4j.writeTxt(origArray, "someArr.txt");
            INDArray readBack = Nd4j.readTxt("someArr.txt");
            assertEquals("Not equal on shape " + ArrayUtils.toString(origArray.shape()), origArray, readBack);
            try {
                Files.delete(Paths.get("someArr.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public char ordering() {
        return 'c';
    }
}
