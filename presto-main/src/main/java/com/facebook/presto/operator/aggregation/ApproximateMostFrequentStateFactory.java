/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.operator.aggregation;

import com.facebook.presto.array.ObjectBigArray;
import com.facebook.presto.operator.aggregation.state.AbstractGroupedAccumulatorState;
import com.facebook.presto.spi.function.AccumulatorStateFactory;

import static java.util.Objects.requireNonNull;

public class ApproximateMostFrequentStateFactory
        implements AccumulatorStateFactory<ApproximateMostFrequentState>
{
    @Override
    public ApproximateMostFrequentState createSingleState()
    {
        return new SingleApproximateMostFrequentState();
    }

    @Override
    public Class<? extends ApproximateMostFrequentState> getSingleStateClass()
    {
        return SingleApproximateMostFrequentState.class;
    }

    @Override
    public ApproximateMostFrequentState createGroupedState()
    {
        return new GroupedApproximateMostFrequentState();
    }

    @Override
    public Class<? extends ApproximateMostFrequentState> getGroupedStateClass()
    {
        return GroupedApproximateMostFrequentState.class;
    }

    public static class GroupedApproximateMostFrequentState
            extends AbstractGroupedAccumulatorState
            implements ApproximateMostFrequentState
    {
        private final ObjectBigArray<TypedApproximateMostFrequentHistogram> typedHistogram = new ObjectBigArray<>();
        private long size;

        @Override
        public void ensureCapacity(long size)
        {
            typedHistogram.ensureCapacity(size);
        }

        @Override
        public TypedApproximateMostFrequentHistogram get()
        {
            return typedHistogram.get(getGroupId());
        }

        @Override
        public void set(TypedApproximateMostFrequentHistogram value)
        {
            requireNonNull(value, "value is null");

            TypedApproximateMostFrequentHistogram previous = get();
            if (previous != null) {
                size -= previous.getEstimatedSize();
            }

            typedHistogram.set(getGroupId(), value);
            size += value.getEstimatedSize();
        }

        @Override
        public void addMemoryUsage(long memory)
        {
            size += memory;
        }

        @Override
        public long getEstimatedSize()
        {
            return size + typedHistogram.sizeOf();
        }
    }

    public static class SingleApproximateMostFrequentState
            implements ApproximateMostFrequentState
    {
        private TypedApproximateMostFrequentHistogram typedHistogram;

        @Override
        public TypedApproximateMostFrequentHistogram get()
        {
            return typedHistogram;
        }

        @Override
        public void set(TypedApproximateMostFrequentHistogram value)
        {
            typedHistogram = value;
        }

        @Override
        public void addMemoryUsage(long memory)
        {
        }

        @Override
        public long getEstimatedSize()
        {
            if (typedHistogram == null) {
                return 0;
            }
            return typedHistogram.getEstimatedSize();
        }
    }
}
