/*
 * Copyright 2023 Erfan Sn
 *
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

package ir.erfansn.artouch.benchmark

import android.graphics.PixelFormat
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.platform.app.InstrumentationRegistry
import ir.erfansn.artouch.benchmark.di.testModule
import ir.erfansn.artouch.benchmark.util.loadJpegImageIntoImageProxy
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.di.HAND_DETECTOR_QUALIFIER
import ir.erfansn.artouch.producer.di.producerModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

class HandDetectorBenchmark : KoinTest {

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(producerModule, testModule)
    }

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

    @Test
    fun detectingBenchmark() {
        val handLandmarkerDetector = get<ObjectDetector<HandDetectionResult>>(HAND_DETECTOR_QUALIFIER)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val handImage = context.loadJpegImageIntoImageProxy("hand.jpg", format = PixelFormat.RGBA_8888)

        benchmarkRule.measureRepeated {
            runBlocking {
                val result = launch { handLandmarkerDetector.result.first() }
                runWithTimingDisabled { delay(1000) }
                handLandmarkerDetector.detect(handImage)
                result.join()
            }
        }
    }
}

