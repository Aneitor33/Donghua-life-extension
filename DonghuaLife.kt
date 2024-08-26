package com.cloudstream.plugins

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class DonghuaLife : MainAPI() {
    override var name = "Donghua Life"
    override var mainUrl = "https://donghualife.com"
    override var lang = "es"
    override var supportsLatest = true

    // Búsqueda de series en Donghua Life
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/series/$query/"
        val document = app.get(url).document
        val series = document.select("div.series-item")?.mapNotNull {
            val title = it.select("h2.title").text()
            val href = it.select("a").attr("href")
            val posterUrl = it.select("img").attr("src")

            if (title != null && href != null) {
                TvSeriesSearchResponse(
                    title,
                    href,
                    this.name,
                    TvType.Anime,
                    posterUrl,
                    null
                )
            } else null
        }
        return series ?: listOf()
    }

    // Obtener detalles de una serie
    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.select("h1.title").text()
        val posterUrl = document.select("div.poster img").attr("src")
        val episodes = document.select("div.episodes a").map {
            val episodeUrl = it.attr("href")
            val episodeTitle = it.select("span.episode-number").text()
            Episode(
                episodeTitle,
                episodeUrl
            )
        }

        return TvSeriesLoadResponse(
            title,
            url,
            this.name,
            TvType.Anime,
            episodes,
            posterUrl,
            null,
            null,
            null
        )
    }

    // Obtener enlaces de streaming
    override suspend fun loadLinks(episode: Episode): List<Video> {
        val document = app.get(episode.url).document
        val videoUrls = document.select("iframe").map {
            it.attr("src")
        }

        return videoUrls.map { Video(it, "Server", it, null) }
    }
}