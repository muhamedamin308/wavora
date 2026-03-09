package com.wavora.app.data.local.entity.mappers

import com.wavora.app.data.local.entity.AlbumEntity
import com.wavora.app.data.local.entity.ArtistEntity
import com.wavora.app.data.local.entity.PlaylistEntity
import com.wavora.app.data.local.entity.SongEntity
import com.wavora.app.domain.model.Album
import com.wavora.app.domain.model.Artist
import com.wavora.app.domain.model.Playlist
import com.wavora.app.domain.model.PlaylistType
import com.wavora.app.domain.model.Song

/**
 * @author Muhamed Amin Hassan on 09,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

fun SongEntity.toDomain() = Song(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artistName = artistName,
    albumName = albumName,
    albumId = albumId,
    artistId = artistId,
    duration = durationMs,
    path = path,
    contentUri = contentUri,
    albumArtUri = albumArtUri,
    dateAdded = dateAdded,
    trackNumber = trackNumber,
    year = year,
    bitrate = bitrate,
    mimeType = mimeType,
    size = sizeBytes,
    isFavorite = isFavorite,
    playCount = playCount,
)

fun Song.toEntity() = SongEntity(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    titleKey = title.lowercase(),
    artistName = artistName,
    artistId = artistId,
    albumName = albumName,
    albumId = albumId,
    durationMs = duration,
    path = path,
    contentUri = contentUri,
    albumArtUri = albumArtUri,
    dateAdded = dateAdded,
    trackNumber = trackNumber,
    year = year,
    bitrate = bitrate,
    mimeType = mimeType,
    sizeBytes = size,
    isFavorite = isFavorite,
    playCount = playCount,
)

fun PlaylistEntity.toDomain(
    songCount: Int,
    totalDuration: Long,
) = Playlist(
    id = id,
    name = name,
    songCount = songCount,
    totalDuration = totalDuration,
    createdAt = createdAt,
    updatedAt = updatedAt,
    type = PlaylistType.valueOf(type),
    thumbnailUri = thumbnailUri,
)

fun AlbumEntity.toDomain() = Album(
    id = id,
    title = title,
    artistName = artistName,
    artistId = artistId,
    albumArtUri = albumArtUri,
    songCount = songCount,
    year = year,
    totalDuration = totalDurationMs,
)

fun ArtistEntity.toDomain() = Artist(
    id = id,
    name = name,
    albumCount = albumCount,
    songCount = songCount,
    thumbnailUri = thumbnailUri,
)