package com.phooper.goodlooker.parser

import com.example.delegateadapter.delegate.diff.IComparableItem
import com.phooper.goodlooker.ui.widgets.recyclerview.model.*
import org.jsoup.Jsoup

class Parser {

    fun parseNews(url: String): MutableList<IComparableItem> {
        val listNews = mutableListOf<IComparableItem>()
        val element =
            Jsoup.connect(
                url
            )
                .get()
                .select("article[class=item-cat_site],[class=item-cat_site last-element]")

        for (i in 0 until element.size) {

            val title = element.select("div[class=title-cat_site]")
                .select("span")
                .eq(i)
                .text()

            val linkImage =
                element.select("div[class=thumb-cat_site]")
                    .select("img")
                    .eq(i)
                    .attr("src")

            val linkPost =
                element.select("div[class=thumb-cat_site]")
                    .select("a")
                    .eq(i)
                    .attr("href")

            val date = element.select("meta[itemprop=datePublished]")
                .eq(i)
                .attr("content").substring(0, 10).split("-").asReversed().joinToString(".")

            val comments = element.select("div[class=meta-st-item_posts]")
                .select("div[class=comments-st_posts]")
                .eq(i)
                .text()

            val views = element.select("div[class=meta-st-item_posts]")
                .select("div[class=views-st_posts]")
                .eq(i)
                .text()

            listNews.add(
                PostItemViewModel(
                    title, date, comments, views, linkImage, linkPost
                )
            )
        }
        return listNews
    }

    fun parsePost(url: String): MutableList<IComparableItem> {

        val listPost = mutableListOf<IComparableItem>()

        Jsoup.connect(
            url
        )
            .get()
            .select("article[class=article-content_vn]")
            .select("p, h1, h2, h3, ol, ul, div[class=article-header_meta]")
            .forEach { it ->
                when {
                    //Header 1
                    (it.select("h1").isNotEmpty()) -> {
                        listPost.add(H1ItemViewModel(it.text()))
                    }
                    //Meta
                    (it.select("div[class=article-header_meta]").isNotEmpty()) -> {
                        listPost.add(
                            MetaItemViewModel(
                                it.select("div[class=article-header_meta]").text().substring(
                                    14,
                                    24
                                ),
                                it.select("div[class=article-header_meta]").text().substring(
                                    42,
                                    44
                                ) + " мин."
                            )
                        )
                    }
                    //Header 2
                    (it.select("h2").isNotEmpty()) -> {
                        listPost.add(H2ItemViewModel(it.text()))
                    }
                    //Header 3
                    (it.select("h3").isNotEmpty()) -> {
                        listPost.add(H3ItemViewModel(it.text()))
                    }
                    //Button
                    (it.select("p").select("a").attr("class").contains("button")) -> {
                        listPost.add(ButtonItemViewModel(it.text(), it.select("a").attr("href")))
                    }
                    //Image
                    (it.select("p").select("img")).isNotEmpty() -> {
                        listPost.add(ImageItemViewModel(it.select("img").attr("src")))
                    }
                    //Unordered list
                    (it.select("ul").isNotEmpty()) -> {
                        it.select("ul").select("li").forEach {
                            listPost.add(ULItemViewModel(it.text()))
                        }
                    }
                    //Ordered list
                    (it.select("ol").isNotEmpty()) -> {
                        var count = 1
                        it.select("ol").select("li").forEach {
                            listPost.add(OLItemViewModel((count++).toString() + ")", it.text()))
                        }
                    }
                    //YouTube link
                    ((it.select("p").select("iframe").attr("src")).isNotEmpty()) -> {
                        //TODO: Regex for youtube link
                        listPost.add(
                            YoutubeItemViewModel(
                                (("(?im)\\b(?:https?:\\/\\/)?(?:w{3}\\.)?youtu(?:be)?\\.(?:com|be)\\/(?:(?:\\??v=?i?=?\\/?)|watch\\?vi?=|watch\\?.*?&v=|embed\\/|)([A-Z0-9_-]{11})\\S*(?=\\s|\$)")
                                    .toRegex().find(
                                        it.select("iframe").attr("src")
                                    )?.groupValues!![1]
                                        )
                            )
                        )

                    }
                    //Paragraph
                    ((it.select("p").select("img")).isEmpty() && it.select("p").isNotEmpty()) -> {
                        listPost.add(PItemViewModel(it.text()))
                    }
                }

            }

        return listPost
    }
}