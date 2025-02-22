package ai.platon.pulsar.ql

import kotlin.test.Ignore
import kotlin.test.Test

class TestManual : TestBase() {

    @Test
    fun load() {
        execute("CALL DOM_LOAD('$productIndexUrl')")
    }

    /**
     * Extract by css selectors, {@link http://www.w3school.com.cn/cssref/css_selectors.asp}
     * */
    @Test
    fun extractByCss() {
        execute("SELECT * FROM LOAD_AND_SELECT('$productIndexUrl', '.welcome')")
    }

    @Test
    fun projectFields() {
        execute("SELECT DOM_TEXT(DOM) FROM LOAD_AND_SELECT('$productIndexUrl', '.welcome')")
    }

    @Test
    fun extractByCssBox() {
        execute("SELECT * FROM LOAD_AND_SELECT('$productIndexUrl', '*:in-box(*,*,229,36)')")
        execute("SELECT IN_BOX_FIRST_TEXT(DOM_LOAD('$productIndexUrl'), '229x36')")
    }

    @Test
    fun extractByCssExpression() {
        execute("SELECT * FROM LOAD_AND_SELECT('$productIndexUrl', '*:expr(width==248 && height==228)')")
        execute("SELECT DOM_TITLE(DOM) FROM LOAD_AND_SELECT('$productIndexUrl', '*:expr(width==248 && height==228) a')")
    }

    @Test
    fun extractBySql() {
        execute("SELECT DOM_TITLE(DOM) FROM LOAD_AND_SELECT('$productIndexUrl', '.nfPic a')")
        execute("SELECT DOM_TITLE(DOM) FROM LOAD_AND_SELECT('$productIndexUrl', '.nfPic a') WHERE LOCATE('白金版', DOM_TITLE(DOM)) > 0")

        execute("SELECT * FROM LOAD_AND_GET_FEATURES('$productIndexUrl') WHERE WIDTH=248 AND HEIGHT=228 LIMIT 100")
    }

    @Test
    fun loadAndGetLinks() {
        execute("SELECT LOAD_AND_SELECT('$productIndexUrl', '.nfList a')")
        execute("SELECT DOM_ABS_HREF(DOM) FROM LOAD_AND_SELECT('$productIndexUrl', '.nfList a')")
    }

    @Test
    fun loadAndGetLinksWithCssExpression() {
        val expr = "width > 240 && width < 250 && height > 360 && height < 370"
        execute("SELECT DOM_ABS_HREF(DOM) FROM LOAD_AND_SELECT('$productIndexUrl', '*:expr($expr) a')")
    }

    @Test
    fun testLoadAndGetLinksWithSqlCondition() {
        execute("""SELECT *
            FROM LOAD_AND_GET_FEATURES('$productIndexUrl')
            WHERE WIDTH BETWEEN 240 AND 250 AND HEIGHT BETWEEN 360 AND 370 LIMIT 10""")

        execute("""SELECT DOM_ABS_HREF(DOM_SELECT_FIRST(DOM, 'a')) AS HREF
            FROM LOAD_AND_GET_FEATURES('$productIndexUrl')
            WHERE WIDTH BETWEEN 240 AND 250 AND HEIGHT BETWEEN 360 AND 370 LIMIT 10""")

        execute("""SELECT DOM_ABS_HREF(DOM_SELECT_FIRST(DOM, 'a')) AS HREF
            FROM LOAD_AND_GET_FEATURES('$productIndexUrl')
            WHERE SIBLING > 250 LIMIT 10""")
    }

    @Test
    fun loadAndGetLinksUsingPreDefinedFunction() {
        val expr = "width > 240 && width < 250 && height > 360 && height < 370"
        execute("SELECT * FROM LOAD_AND_GET_LINKS('$productIndexUrl', '*:expr($expr)')")
    }

    @Test
    fun loadOutPages() {
        execute("SELECT DOM, DOM_TEXT(DOM) FROM LOAD_AND_SELECT('$productIndexUrl', '.nfList')")
    }

    @Test
    @Ignore("TimeConsumingTest")
    fun loadOutPagesUsingPreDefinedFunction() {
        val expr = "width > 240 && width < 250 && height > 360 && height < 370"
        execute("CALL SET_PAGE_EXPIRES('1s', 1)")
        execute("SELECT DOM, DOM_TEXT(DOM) FROM LOAD_OUT_PAGES('$productIndexUrl', '*:expr($expr)', 1, 20)")
    }

    @Test
    fun loadAndGetFeatures() {
        execute("SELECT * FROM LOAD_AND_GET_FEATURES('$productIndexUrl', '.nfList', 1, 20)")
        execute("SELECT * FROM LOAD_AND_GET_FEATURES('$productDetailUrl', 'DIV,UL,UI,P', 1, 20)")
    }

    /**
     * Get vivid links - the most interested links in a page, for example, product list or news list
     * */
    @Test
    fun getVividLinks() {
        val expr = "sibling > 20 && char > 40 && char < 100 && width > 200"
        execute("""SELECT
            DOM, DOM_FIRST_HREF(DOM), TOP, LEFT, WIDTH, HEIGHT, CHAR, IMG, A, SIBLING, DOM_TEXT(DOM)
            FROM LOAD_AND_GET_FEATURES('$productIndexUrl', '*:expr($expr)')
            ORDER BY SIBLING DESC, CHAR DESC LIMIT 500""")
    }

    /**
     * Get the container element for all vivid links
     * */
    @Test
    fun getVividLinkParent() {
        val expr = "sibling > 20 && char > 40 && char < 100 && width > 200"
        execute("""SELECT
            DOM_PARENT(DOM), DOM, DOM_FIRST_HREF(DOM), TOP, LEFT, WIDTH, HEIGHT, CHAR, IMG, A, SIBLING, DOM_TEXT(DOM)
            FROM LOAD_AND_GET_FEATURES('$productIndexUrl', '*:expr($expr)')
            ORDER BY SIBLING DESC, CHAR DESC LIMIT 50""")
    }

    /**
     * Get the element who has the most direct children, it probably be a container of vivid links
     * */
    @Test
    fun getElementWithMostChildren() {
        val expr = "child > 20 && char > 100 && width > 800"
        execute("""SELECT
            DOM, DOM_FIRST_HREF(DOM), TOP, LEFT, WIDTH, HEIGHT, CHAR, IMG, A, CHILD, SIBLING
            FROM LOAD_AND_GET_FEATURES('$productIndexUrl', '*:expr($expr)')
            ORDER BY CHILD DESC, CHAR DESC LIMIT 50""")
    }

    /**
     * Extracting by box is the simplest method to extract text from Web pages
     * */
    @Ignore("Disable box feature, it's never be used")
    @Test
    fun extractByBox() {
        val restrictCss = "*:expr(img>0 && width>200 && height>200 && sibling>30)"

        val sql = """
SELECT
  IN_BOX_FIRST_TEXT(DOM, '560x27') AS TITLE,
  IN_BOX_FIRST_TEXT(DOM, '570x36') AS PRICE1,
  IN_BOX_FIRST_TEXT(DOM, '560x56') AS TITLE2,
  IN_BOX_FIRST_TEXT(DOM, '570x85') AS PRICE2,
  DOM_BASE_URI(DOM) AS URI,
  IN_BOX_FIRST_IMG(DOM, '405x405') AS MAIN_IMAGE,
  DOM_IMG(DOM) AS NIMG
FROM LOAD_OUT_PAGES('$productIndexUrl', '$restrictCss', 1, 10)
WHERE DOM_CH(DOM) > 100;
"""
        execute(sql)
    }

    /**
     * Extracting by box is the simplest method to extract text from Web pages
     * */
    @Ignore("Disable box feature, it's never be used")
    @Test
    fun extractByBox2() {
        val restrictCss = "*:expr(img>0 && width>200 && height>200 && sibling>=40)"

        val sql = """
SELECT
  DOM_FIRST_TEXT(DOM, 'div:iN-bOx(560,27),*:IN-BOX(560,56)') AS TITLE,
  DOM_FIRST_TEXT(DOM, '.brand') AS TITLE2,
  DOM_WIDTH(DOM_SELECT_FIRST(DOM, '.brand')) AS W,
  DOM_HEIGHT(DOM_SELECT_FIRST(DOM, '.brand')) AS H,
  IN_BOX_FIRST_TEXT(DOM, '560x27,560x56') AS TITLE3
FROM LOAD_OUT_PAGES('$productIndexUrl', '$restrictCss', 1, 10)
WHERE DOM_CH(DOM) > 100
"""
        execute(sql)
    }
    
    @Ignore("Mia.com is closed")
    @Test
    fun testSqlVariables() {
        val sql = """
SET @LINK='https://www.mia.com/formulas.html';
SET @OUT_LINK_STRICT_CSS='*:expr(img>0 && width>200 && height>200 && sibling>30)';

-- Show page features to see where are the useful links
-- SELECT * FROM DOMT_LOAD_AND_GET_FEATURES(@LINK, @OUT_LINK_STRICT_CSS) LIMIT 100;

SELECT
  DOM_FIRST_TEXT(DOM, '.brand') AS TITLE,
  DOM_FIRST_TEXT(DOM, '.pbox_price') AS PRICE,
  DOM_BASE_URI(DOM) AS URI,
  IN_BOX_FIRST_IMG(DOM, '405x405') AS MAIN_IMAGE,
  DOM_FIRST_TEXT(DOM, '#wrap_con') AS PARAMETERS_TEXT,
  DOM_CH(DOM) AS NCHAR,
  DOM_IMG(DOM) AS NIMG
FROM LOAD_OUT_PAGES(@LINK, @OUT_LINK_STRICT_CSS, 1, 100)
WHERE DOM_CH(DOM) > 100;
        """.trimIndent()
        execute(sql)
    }

    /**
     * A simple Web page monitor, monitoring news
     * */
    @Test
    fun monitorNewsColumnForQQ() {
        val portal = "http://news.qq.com/world_index.shtml"

        // Tencent news have a redirect mechanism, we have to fix this
        execute("SELECT DOM, TOP, LEFT, WIDTH, HEIGHT, IMG, A, SIBLING, DOM_TEXT(DOM), DOM_FIRST_HREF(DOM) " +
                "FROM LOAD_AND_GET_FEATURES('$portal') " +
                "WHERE SIBLING>20 AND DOM_TEXT_LENGTH(DOM) > 10 AND TOP > 300 AND TOP < 3000")

        val detail = "http://new.qq.com/omn/20180424/20180424A104ZC.html"
        execute("SELECT DOM, TOP, LEFT, WIDTH, HEIGHT, IMG, A, CHAR, SIBLING, DOM_TEXT(DOM), DOM_FIRST_HREF(DOM) " +
                "FROM LOAD_AND_GET_FEATURES('$detail') WHERE SEQ > 170 AND SEQ < 400")

        val sql = """
SELECT
  DOM_FIRST_TEXT(DOM, 'H1') AS TITLE,
  DOM_FIRST_TEXT(DOM, '#LeftTool') AS DATE_TIME,
  DOM_FIRST_TEXT(DOM, '.content-article') AS CONTENT
FROM LOAD_OUT_PAGES('$portal', '.Q-tpList', 1, 10)
"""
        execute(sql)
    }

    /**
     * A simple Web page monitor, monitoring news
     * */
    @Test
    fun monitorNewsColumnForCnHuBei() {
        val portal = "http://news.cnhubei.com/"

        execute("SELECT DOM, TOP, LEFT, WIDTH, HEIGHT, IMG, A, SIBLING, DOM_TEXT(DOM), DOM_FIRST_HREF(DOM) " +
                "FROM LOAD_AND_GET_FEATURES('$portal') " +
                "WHERE SIBLING>30 AND DOM_TEXT_LENGTH(DOM) > 10 AND TOP > 300 AND TOP < 3000")

        val detail = "http://news.cnhubei.com/xw/jj/201804/t4102239.shtml"
        execute("SELECT DOM, TOP, LEFT, WIDTH, HEIGHT, IMG, A, CHAR, SIBLING, DOM_TEXT(DOM), DOM_FIRST_HREF(DOM) " +
                "FROM LOAD_AND_GET_FEATURES('$detail') WHERE SEQ > 170 AND SEQ < 400")

        val sql = """
SELECT
  DOM_FIRST_TEXT(DOM, 'H1') AS TITLE,
  DOM_FIRST_TEXT(DOM, '.jcwsy_mini_content') AS DATE_TIME,
  DOM_FIRST_TEXT(DOM, '.content_box') AS CONTENT
FROM LOAD_OUT_PAGES('$portal', '.news_list_box', 1, 10)
"""
        execute(sql)
    }

    /**
     * A simple Web page monitor, monitoring products
     * */
    @Test
    @Ignore("TimeConsumingTest")
    fun monitorProductColumn() {
        val restrictCss = "*:expr(img>0 && width>200 && height>200 && sibling>30)"
        val titleExpr =
            "TOP>=287 && TOP<=307 && LEFT==472 && width==560 && height>=27 && height<=54 && char>=34 && char<=41"

        val sql = """
SELECT
  DOM_FIRST_TEXT(DOM, 'div:iN-bOx(560,27),*:IN-BOX(560,56)') AS TITLE,
  DOM_FIRST_TEXT(DOM, '*:expr($titleExpr)') AS TITLE2,
  IN_BOX_FIRST_TEXT(DOM, '560x27,560x56') AS TITLE3
FROM LOAD_OUT_PAGES('$productIndexUrl', '$restrictCss', 1, 10)
WHERE DOM_CH(DOM) > 100
"""
        execute(sql)
    }

    /**
     * A simple Web page monitor, monitoring products
     * */
    @Test
    @Ignore("TimeConsumingTest")
    fun monitorProductColumn2() {
        execute("SELECT DOM, TOP, LEFT, WIDTH, HEIGHT, IMG, A, SIBLING, DOM_TEXT(DOM), DOM_FIRST_HREF(DOM) " +
                "FROM LOAD_AND_GET_FEATURES('$productIndexUrl') " +
                "WHERE SIBLING>30 AND DOM_TEXT_LENGTH(DOM) > 10 AND TOP > 300 AND TOP < 3000")

        execute("SELECT DOM, TOP, LEFT, WIDTH, HEIGHT, IMG, A, CHAR, SIBLING, DOM_TEXT(DOM), DOM_FIRST_HREF(DOM) " +
                "FROM LOAD_AND_GET_FEATURES('$productDetailUrl') WHERE SEQ > 170 AND SEQ < 400")

        val restrictCss = "*:expr(img>0 && width>200 && height>200 && sibling>30)"

        val sql = """
SELECT
  DOM_FIRST_TEXT(DOM, 'div:iN-bOx(560,27),*:IN-BOX(560,56)') AS TITLE,
  IN_BOX_FIRST_TEXT(DOM, '560x27,560x56') AS TITLE3
FROM LOAD_OUT_PAGES('$productIndexUrl', '$restrictCss', 1, 20)
WHERE DOM_CH(DOM) > 100
"""
        execute(sql)
    }
}
