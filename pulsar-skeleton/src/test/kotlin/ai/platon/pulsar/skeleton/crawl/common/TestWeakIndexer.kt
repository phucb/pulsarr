package ai.platon.pulsar.skeleton.crawl.common

import ai.platon.pulsar.common.DateTimes
import ai.platon.pulsar.common.config.AppConstants
import ai.platon.pulsar.common.config.CapabilityTypes
import ai.platon.pulsar.common.config.MutableConfig
import ai.platon.pulsar.common.urls.UrlUtils.reverseUrlOrEmpty
import ai.platon.pulsar.persist.WebDb

import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.test.*

/**
 * Created by vincent on 16-7-20.
 * Copyright @ 2013-2016 Platon AI. All rights reserved
 *
 * TODO: Test failed
 */
@Ignore("TODO: Test failed")
class TestWeakIndexer {
    private val LOG = LoggerFactory.getLogger(TestWeakIndexer::class.java)
    private val conf = MutableConfig().also { it[CapabilityTypes.STORAGE_CRAWL_ID] = "test" }
    private val webDb = WebDb(conf)
    private val urlTrackerIndexer = WeakPageIndexer(AppConstants.URL_TRACKER_HOME_URL, webDb)
    private val store = webDb.dataStorageFactory.getOrCreatePageStore()
    private val exampleUrls = IntRange(10000, 10050).map { AppConstants.EXAMPLE_URL + "/" + it }
    private var exampleUrl = AppConstants.EXAMPLE_URL + "/" + DateTimes.format(Instant.now(), "MMdd")

    @BeforeTest
    fun setup() {
    }

    @AfterTest
    fun teardown() {
        webDb.flush()
        webDb.close()
        LOG.debug("In shell: \nget '{}', '{}'", store.schemaName, reverseUrlOrEmpty(exampleUrl))
    }

    @Ignore("TODO: Test failed")
    @Test
    fun testWeakPageIndexer() {
        val pageNo = 1
        val indexPageUrl = AppConstants.URL_TRACKER_HOME_URL + "/" + pageNo
        webDb.delete(indexPageUrl)
        webDb.flush()
        urlTrackerIndexer.indexAll(exampleUrls)
        urlTrackerIndexer.commit()
        var page = webDb.get(indexPageUrl)
        assertTrue(page.isNotNil)
        assertTrue(page.isInternal)
        assertEquals(exampleUrls.size.toLong(), page.liveLinks.size.toLong())
        urlTrackerIndexer.takeAll(pageNo)
        page = webDb.get(indexPageUrl)
        assertTrue(page.isNotNil)
        assertTrue(page.liveLinks.isEmpty())
    }
}
