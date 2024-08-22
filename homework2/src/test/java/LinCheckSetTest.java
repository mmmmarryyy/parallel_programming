import org.jetbrains.kotlinx.lincheck.LinChecker;
import org.jetbrains.kotlinx.lincheck.Options;
import org.jetbrains.kotlinx.lincheck.annotations.Operation;
import org.jetbrains.kotlinx.lincheck.annotations.Param;
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest;
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions;
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Param(name = "value", gen = IntGen.class, conf = "1:5")
@StressCTest
@ModelCheckingCTest
public class LinCheckSetTest {
    private Set<Integer> set = new SetImpl<>();

    @Operation
    public boolean add(@Param(name = "value") int value) {
        return set.add(value);
    }

    @Operation
    public boolean remove(@Param(name = "value") int value) {
        return set.remove(value);
    }

    @Operation
    public boolean contains(@Param(name = "value") int value) {
        return set.contains(value);
    }

    @Operation
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Operation
    public List<Integer> iterator() {
        Iterator<Integer> iterator = set.iterator();
        ArrayList<Integer> values = new ArrayList<>();
        while (iterator.hasNext()) {
            values.add(iterator.next());
        }
        return values;
    }

    @Test
    public void testWithoutOptions() {
        LinChecker.check(LinCheckSetTest.class);
    }

    @Test
    public void stressTest() {
        Options options = new StressOptions();
        LinChecker.check(LinCheckSetTest.class, options);
    }

    @Test
    public void modelCheckingTest() {
        ModelCheckingOptions options = new ModelCheckingOptions();
        LinChecker.check(LinCheckSetTest.class, options);
    }
}
