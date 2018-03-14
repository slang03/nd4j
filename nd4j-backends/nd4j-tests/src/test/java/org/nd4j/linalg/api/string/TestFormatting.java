package org.nd4j.linalg.api.string;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.string.NDArrayStrings;

/**
 * @author Adam Gibson
 */
@Slf4j
@RunWith(Parameterized.class)
public class TestFormatting extends BaseNd4jTest {

    public TestFormatting(Nd4jBackend backend) {
        super(backend);
    }

    @Test
    public void testTwoByTwo() {
        INDArray arr = Nd4j.create(2, 2, 2, 2);
        System.out.println(new NDArrayStrings().format(arr));

    }

    @Test
    public void testNd4jArrayString() {

        INDArray arr = Nd4j.create(new float[]{1f, 20000000f, 40.838383f, 3f}, new int[]{2, 2});

        String expected1 = "[[1.000E0,4.084E1],\n" + " [2.000E7,3.000E0]]";
        String serializedData1 = new NDArrayStrings(",", 3).format(arr);
        Assert.assertEquals(expected1.replaceAll(" ", ""), serializedData1.replaceAll(" ", ""));

        String expected2 = "[[1.0000E0,4.0838E1],\n" + " [2.0000E7,3.0000E0]]";
        String serializedData2 = new NDArrayStrings().format(arr);
        Assert.assertEquals(expected2.replaceAll(" ", ""), serializedData2.replaceAll(" ", ""));

        String expected3 = "[[100.00E-2,408.3838E-1],\n" + " [200.00E5,300.00E-2]]";
        String serializedData3 = new NDArrayStrings(",", "000.00##E0").format(arr);
        Assert.assertEquals(expected3.replaceAll(" ", ""), serializedData3.replaceAll(" ", ""));
    }

    @Override
    public char ordering() {
        return 'f';
    }
}
