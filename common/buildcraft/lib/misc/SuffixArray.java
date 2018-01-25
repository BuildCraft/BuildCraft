package buildcraft.lib.misc;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class SuffixArray<T>
{
    /**
     * A debug property (<code>SuffixArray.printComparisons</code>) that can be specified in the JVM arguments, that
     * causes debug printing of comparisons as they happen.
     */
    private static final boolean DEBUG_PRINT_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
    /**
     * A debug property (<code>SuffixArray.printArray</code>) that can be specified in the JVM arguments, that causes
     * the full array to be printed ({@link #printArray()}) after calling {@link #generate()}
     */
    private static final boolean DEBUG_PRINT_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
    private static final Logger LOGGER = LogManager.getLogger();
    protected final List<T> list = Lists.<T>newArrayList();
    private final IntList chars = new IntArrayList();
    private final IntList wordStarts = new IntArrayList();
    private IntList suffixToT = new IntArrayList();
    private IntList offsets = new IntArrayList();
    private int maxStringLength;

    public void add(T p_194057_1_, String p_194057_2_)
    {
        this.maxStringLength = Math.max(this.maxStringLength, p_194057_2_.length());
        int i = this.list.size();
        this.list.add(p_194057_1_);
        this.wordStarts.add(this.chars.size());

        for (int j = 0; j < p_194057_2_.length(); ++j)
        {
            this.suffixToT.add(i);
            this.offsets.add(j);
            this.chars.add(p_194057_2_.charAt(j));
        }

        this.suffixToT.add(i);
        this.offsets.add(p_194057_2_.length());
        this.chars.add(-1);
    }

    public void generate()
    {
        int i = this.chars.size();
        int[] aint = new int[i];
        final int[] aint1 = new int[i];
        final int[] aint2 = new int[i];
        int[] aint3 = new int[i];
        IntComparator intcomparator = new IntComparator()
        {
            public int compare(int p_compare_1_, int p_compare_2_)
            {
                return aint1[p_compare_1_] == aint1[p_compare_2_] ? Integer.compare(aint2[p_compare_1_], aint2[p_compare_2_]) : Integer.compare(aint1[p_compare_1_], aint1[p_compare_2_]);
            }
            public int compare(Integer p_compare_1_, Integer p_compare_2_)
            {
                return this.compare(p_compare_1_.intValue(), p_compare_2_.intValue());
            }
        };
        Swapper swapper = (p_194054_3_, p_194054_4_) ->
        {

            if (p_194054_3_ != p_194054_4_)
            {
                int i2 = aint1[p_194054_3_];
                aint1[p_194054_3_] = aint1[p_194054_4_];
                aint1[p_194054_4_] = i2;
                i2 = aint2[p_194054_3_];
                aint2[p_194054_3_] = aint2[p_194054_4_];
                aint2[p_194054_4_] = i2;
                i2 = aint3[p_194054_3_];
                aint3[p_194054_3_] = aint3[p_194054_4_];
                aint3[p_194054_4_] = i2;
            }
        };

        for (int j = 0; j < i; ++j)
        {
            aint[j] = this.chars.getInt(j);
        }

        int k1 = 1;

        for (int k = Math.min(i, this.maxStringLength); k1 * 2 < k; k1 *= 2)
        {
            for (int l = 0; l < i; aint3[l] = l++)
            {
                aint1[l] = aint[l];
                aint2[l] = l + k1 < i ? aint[l + k1] : -2;
            }

            Arrays.quickSort(0, i, intcomparator, swapper);

            for (int l1 = 0; l1 < i; ++l1)
            {
                if (l1 > 0 && aint1[l1] == aint1[l1 - 1] && aint2[l1] == aint2[l1 - 1])
                {
                    aint[aint3[l1]] = aint[aint3[l1 - 1]];
                }
                else
                {
                    aint[aint3[l1]] = l1;
                }
            }
        }

        IntList intlist1 = this.suffixToT;
        IntList intlist = this.offsets;
        this.suffixToT = new IntArrayList(intlist1.size());
        this.offsets = new IntArrayList(intlist.size());

        for (int i1 = 0; i1 < i; ++i1)
        {
            int j1 = aint3[i1];
            this.suffixToT.add(intlist1.getInt(j1));
            this.offsets.add(intlist.getInt(j1));
        }

        if (DEBUG_PRINT_ARRAY)
        {
            this.printArray();
        }
    }

    /**
     * Prints the entire array to the logger, on debug level
     */
    private void printArray()
    {
        for (int i2 = 0; i2 < this.suffixToT.size(); ++i2)
        {
            LOGGER.debug("{} {}", Integer.valueOf(i2), this.getString(i2));
        }

        LOGGER.debug("");
    }

    private String getString(int p_194059_1_)
    {
        int i2 = this.offsets.getInt(p_194059_1_);
        int j2 = this.wordStarts.getInt(this.suffixToT.getInt(p_194059_1_));
        StringBuilder stringbuilder = new StringBuilder();

        for (int k2 = 0; j2 + k2 < this.chars.size(); ++k2)
        {
            if (k2 == i2)
            {
                stringbuilder.append('^');
            }

            int l2 = ((Integer)this.chars.get(j2 + k2)).intValue();

            if (l2 == -1)
            {
                break;
            }

            stringbuilder.append((char)l2);
        }

        return stringbuilder.toString();
    }

    private int compare(String p_194056_1_, int p_194056_2_)
    {
        int i2 = this.wordStarts.getInt(this.suffixToT.getInt(p_194056_2_));
        int j2 = this.offsets.getInt(p_194056_2_);

        for (int k2 = 0; k2 < p_194056_1_.length(); ++k2)
        {
            int l2 = this.chars.getInt(i2 + j2 + k2);

            if (l2 == -1)
            {
                return 1;
            }

            char c0 = p_194056_1_.charAt(k2);
            char c1 = (char)l2;

            if (c0 < c1)
            {
                return -1;
            }

            if (c0 > c1)
            {
                return 1;
            }
        }

        return 0;
    }

    public List<T> search(String p_194055_1_)
    {
        int i2 = this.suffixToT.size();
        int j2 = 0;
        int k2 = i2;

        while (j2 < k2)
        {
            int l2 = j2 + (k2 - j2) / 2;
            int i3 = this.compare(p_194055_1_, l2);

            if (DEBUG_PRINT_COMPARISONS)
            {
                LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", p_194055_1_, Integer.valueOf(l2), this.getString(l2), Integer.valueOf(i3));
            }

            if (i3 > 0)
            {
                j2 = l2 + 1;
            }
            else
            {
                k2 = l2;
            }
        }

        if (j2 >= 0 && j2 < i2)
        {
            int i4 = j2;
            k2 = i2;

            while (j2 < k2)
            {
                int j4 = j2 + (k2 - j2) / 2;
                int j3 = this.compare(p_194055_1_, j4);

                if (DEBUG_PRINT_COMPARISONS)
                {
                    LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", p_194055_1_, Integer.valueOf(j4), this.getString(j4), Integer.valueOf(j3));
                }

                if (j3 >= 0)
                {
                    j2 = j4 + 1;
                }
                else
                {
                    k2 = j4;
                }
            }

            int k4 = j2;
            IntArrayList intset = new IntArrayList();

            for (int k3 = i4; k3 < k4; ++k3)
            {
                intset.add(this.suffixToT.getInt(k3));
            }

            int[] aint4 = intset.toIntArray();
            java.util.Arrays.sort(aint4);
            Set<T> set = Sets.<T>newLinkedHashSet();

            for (int l3 : aint4)
            {
                set.add(this.list.get(l3));
            }

            return Lists.newArrayList(set);
        }
        else
        {
            return Collections.<T>emptyList();
        }
    }
}