/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.trackselection;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A {@link MappingTrackSelector} that allows configuration of common parameters.
 */
public class DefaultFixedTrackSelector extends MappingTrackSelector {

  /**
   * If a dimension (i.e. width or height) of a video is greater or equal to this fraction of the
   * corresponding viewport dimension, then the video is considered as filling the viewport (in that
   * dimension).
   */
  private static final float FRACTION_TO_CONSIDER_FULLSCREEN = 0.98f;
  private static final int[] NO_TRACKS = new int[0];

  private final TrackSelection.Factory adaptiveVideoTrackSelectionFactory;
  private final int selectedVideoTrackIndex;
  private final int selectedVideoGroupIndex;
  private final int selectedAudioTrackIndex;
  private final int selectedAudioGroupIndex;

  // Audio.
  private String preferredAudioLanguage;

  // Text.
  private String preferredTextLanguage;

  // Video.
  private boolean allowMixedMimeAdaptiveness;
  private boolean allowNonSeamlessAdaptiveness;
  private int maxVideoWidth;
  private int maxVideoHeight;
  private boolean exceedVideoConstraintsIfNecessary;
  private boolean orientationMayChange;
  private int viewportWidth;
  private int viewportHeight;

  /**
   * Constructs an instance that does not support adaptive video.
   *
   * @param eventHandler A handler to use when delivering events to listeners. May be null if
   *     listeners will not be added.
   * @param selectedVideoTrackIndex
   * @param selectedVideoGroupIndex
   * @param selectedAudioTrackIndex
   * @param selectedAudioGroupIndex
   */
  public DefaultFixedTrackSelector(Handler eventHandler, int selectedVideoTrackIndex, int selectedVideoGroupIndex, int selectedAudioTrackIndex, int selectedAudioGroupIndex) {
    this(eventHandler, null, selectedVideoTrackIndex, selectedVideoGroupIndex, selectedAudioTrackIndex, selectedAudioGroupIndex);
  }

  /**
   * Constructs an instance that uses a factory to create adaptive video track selections.
   *
   * @param eventHandler A handler to use when delivering events to listeners. May be null if
   *     listeners will not be added.
   * @param adaptiveVideoTrackSelectionFactory A factory for adaptive video {@link TrackSelection}s,
   *     or null if the selector should not support adaptive video.
   */
  public DefaultFixedTrackSelector(Handler eventHandler,
                                   TrackSelection.Factory adaptiveVideoTrackSelectionFactory,
                                   int selectedVideoTrackIndex,
                                   int selectedVideoGroupIndex,
                                   int selectedAudioTrackIndex,
                                   int selectedAudioGroupIndex) {
    super(eventHandler);
    this.adaptiveVideoTrackSelectionFactory = adaptiveVideoTrackSelectionFactory;
    this.selectedVideoTrackIndex = selectedVideoTrackIndex;
    this.selectedVideoGroupIndex = selectedVideoGroupIndex;
    this.selectedAudioTrackIndex = selectedAudioTrackIndex;
    this.selectedAudioGroupIndex = selectedAudioGroupIndex;
    allowNonSeamlessAdaptiveness = true;
    exceedVideoConstraintsIfNecessary = true;
    maxVideoWidth = Integer.MAX_VALUE;
    maxVideoHeight = Integer.MAX_VALUE;
    viewportWidth = Integer.MAX_VALUE;
    viewportHeight = Integer.MAX_VALUE;
    orientationMayChange = true;
  }

  /**
   * Sets the preferred language for audio, as well as for forced text tracks.
   *
   * @param preferredAudioLanguage The preferred language as defined by RFC 5646. {@code null} to
   *     select the default track, or first track if there's no default.
   */
  public void setPreferredLanguages(String preferredAudioLanguage) {
    preferredAudioLanguage = Util.normalizeLanguageCode(preferredAudioLanguage);
    if (!Util.areEqual(this.preferredAudioLanguage, preferredAudioLanguage)) {
      this.preferredAudioLanguage = preferredAudioLanguage;
      invalidate();
    }
  }

  /**
   * Sets the preferred language for text tracks.
   *
   * @param preferredTextLanguage The preferred language as defined by RFC 5646. {@code null} to
   *     select the default track, or no track if there's no default.
   */
  public void setPreferredTextLanguage(String preferredTextLanguage) {
    preferredTextLanguage = Util.normalizeLanguageCode(preferredTextLanguage);
    if (!Util.areEqual(this.preferredTextLanguage, preferredTextLanguage)) {
      this.preferredTextLanguage = preferredTextLanguage;
      invalidate();
    }
  }

  /**
   * Sets whether to allow selections to contain mixed mime types.
   *
   * @param allowMixedMimeAdaptiveness Whether to allow selections to contain mixed mime types.
   */
  public void allowMixedMimeAdaptiveness(boolean allowMixedMimeAdaptiveness) {
    if (this.allowMixedMimeAdaptiveness != allowMixedMimeAdaptiveness) {
      this.allowMixedMimeAdaptiveness = allowMixedMimeAdaptiveness;
      invalidate();
    }
  }

  /**
   * Sets whether non-seamless adaptation is allowed.
   *
   * @param allowNonSeamlessAdaptiveness Whether non-seamless adaptation is allowed.
   */
  public void allowNonSeamlessAdaptiveness(boolean allowNonSeamlessAdaptiveness) {
    if (this.allowNonSeamlessAdaptiveness != allowNonSeamlessAdaptiveness) {
      this.allowNonSeamlessAdaptiveness = allowNonSeamlessAdaptiveness;
      invalidate();
    }
  }

  /**
   * Sets the maximum allowed size for video tracks.
   *
   * @param maxVideoWidth Maximum allowed width.
   * @param maxVideoHeight Maximum allowed height.
   */
  public void setMaxVideoSize(int maxVideoWidth, int maxVideoHeight) {
    if (this.maxVideoWidth != maxVideoWidth || this.maxVideoHeight != maxVideoHeight) {
      this.maxVideoWidth = maxVideoWidth;
      this.maxVideoHeight = maxVideoHeight;
      invalidate();
    }
  }

  /**
   * Equivalent to {@code setMaxVideoSize(1279, 719)}.
   */
  public void setMaxVideoSizeSd() {
    setMaxVideoSize(1279, 719);
  }

  /**
   * Equivalent to {@code setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE)}.
   */
  public void clearMaxVideoSize() {
    setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /**
   * Sets whether video constraints should be ignored when no selection can be made otherwise.
   *
   * @param exceedVideoConstraintsIfNecessary True to ignore video constraints when no selections
   *     can be made otherwise. False to force constraints anyway.
   */
  public void setExceedVideoConstraintsIfNecessary(boolean exceedVideoConstraintsIfNecessary) {
    if (this.exceedVideoConstraintsIfNecessary != exceedVideoConstraintsIfNecessary) {
      this.exceedVideoConstraintsIfNecessary = exceedVideoConstraintsIfNecessary;
      invalidate();
    }
  }

  /**
   * Sets the target viewport size for selecting video tracks.
   *
   * @param viewportWidth Viewport width in pixels.
   * @param viewportHeight Viewport height in pixels.
   * @param orientationMayChange Whether orientation may change during playback.
   */
  public void setViewportSize(int viewportWidth, int viewportHeight, boolean orientationMayChange) {
    if (this.viewportWidth != viewportWidth || this.viewportHeight != viewportHeight
        || this.orientationMayChange != orientationMayChange) {
      this.viewportWidth = viewportWidth;
      this.viewportHeight = viewportHeight;
      this.orientationMayChange = orientationMayChange;
      invalidate();
    }
  }

  /**
   * Retrieves the viewport size from the provided {@link Context} and calls
   * {@link #setViewportSize(int, int, boolean)} with this information.
   *
   * @param context The context to obtain the viewport size from.
   * @param orientationMayChange Whether orientation may change during playback.
   */
  public void setViewportSizeFromContext(Context context, boolean orientationMayChange) {
    Point viewportSize = Util.getPhysicalDisplaySize(context); // Assume the viewport is fullscreen.
    setViewportSize(viewportSize.x, viewportSize.y, orientationMayChange);
  }

  /**
   * Equivalent to {@code setViewportSize(Integer.MAX_VALUE, Integer.MAX_VALUE, true)}.
   */
  public void clearViewportConstraints() {
    setViewportSize(Integer.MAX_VALUE, Integer.MAX_VALUE, true);
  }

  // MappingTrackSelector implementation.

  @Override
  protected TrackSelection[] selectTracks(RendererCapabilities[] rendererCapabilities,
      TrackGroupArray[] rendererTrackGroupArrays, int[][][] rendererFormatSupports)
      throws ExoPlaybackException {
    // Make a track selection for each renderer.
    TrackSelection[] rendererTrackSelections = new TrackSelection[rendererCapabilities.length];
    for (int i = 0; i < rendererCapabilities.length; i++) {
      switch (rendererCapabilities[i].getTrackType()) {
        case C.TRACK_TYPE_VIDEO:
          rendererTrackSelections[i] = selectVideoTrack(rendererCapabilities[i],
              rendererTrackGroupArrays[i], rendererFormatSupports[i], maxVideoWidth, maxVideoHeight,
              allowNonSeamlessAdaptiveness, allowMixedMimeAdaptiveness, viewportWidth,
              viewportHeight, orientationMayChange, adaptiveVideoTrackSelectionFactory,
              exceedVideoConstraintsIfNecessary);
          break;
        case C.TRACK_TYPE_AUDIO:
          rendererTrackSelections[i] = selectAudioTrack(rendererTrackGroupArrays[i],
              rendererFormatSupports[i], preferredAudioLanguage);
          break;
        case C.TRACK_TYPE_TEXT:
          rendererTrackSelections[i] = selectTextTrack(rendererTrackGroupArrays[i],
              rendererFormatSupports[i], preferredTextLanguage, preferredAudioLanguage);
          break;
        default:
          rendererTrackSelections[i] = selectOtherTrack(rendererCapabilities[i].getTrackType(),
              rendererTrackGroupArrays[i], rendererFormatSupports[i]);
          break;
      }
    }
    return rendererTrackSelections;
  }

  // Video track selection implementation.

  protected TrackSelection selectVideoTrack(RendererCapabilities rendererCapabilities,
      TrackGroupArray groups, int[][] formatSupport, int maxVideoWidth, int maxVideoHeight,
      boolean allowNonSeamlessAdaptiveness, boolean allowMixedMimeAdaptiveness, int viewportWidth,
      int viewportHeight, boolean orientationMayChange,
      TrackSelection.Factory adaptiveVideoTrackSelectionFactory,
      boolean exceedConstraintsIfNecessary) throws ExoPlaybackException {
    TrackSelection selection = null;
    if (adaptiveVideoTrackSelectionFactory != null) {
      selection = selectAdaptiveVideoTrack(rendererCapabilities, groups, formatSupport,
          maxVideoWidth, maxVideoHeight, allowNonSeamlessAdaptiveness,
          allowMixedMimeAdaptiveness, viewportWidth, viewportHeight,
          orientationMayChange, adaptiveVideoTrackSelectionFactory);
    }
    if (selection == null) {
      selection = selectFixedVideoTrack(groups, formatSupport, maxVideoWidth, maxVideoHeight,
          viewportWidth, viewportHeight, orientationMayChange, exceedConstraintsIfNecessary);
    }
    return selection;
  }

  private static TrackSelection selectAdaptiveVideoTrack(RendererCapabilities rendererCapabilities,
      TrackGroupArray groups, int[][] formatSupport, int maxVideoWidth, int maxVideoHeight,
      boolean allowNonSeamlessAdaptiveness, boolean allowMixedMimeAdaptiveness, int viewportWidth,
      int viewportHeight, boolean orientationMayChange,
      TrackSelection.Factory adaptiveVideoTrackSelectionFactory) throws ExoPlaybackException {
    int requiredAdaptiveSupport = allowNonSeamlessAdaptiveness
        ? (RendererCapabilities.ADAPTIVE_NOT_SEAMLESS | RendererCapabilities.ADAPTIVE_SEAMLESS)
        : RendererCapabilities.ADAPTIVE_SEAMLESS;
    boolean allowMixedMimeTypes = allowMixedMimeAdaptiveness
        && (rendererCapabilities.supportsMixedMimeTypeAdaptation() & requiredAdaptiveSupport) != 0;
    for (int i = 0; i < groups.length; i++) {
      TrackGroup group = groups.get(i);
      int[] adaptiveTracks = getAdaptiveTracksForGroup(group, formatSupport[i],
          allowMixedMimeTypes, requiredAdaptiveSupport, maxVideoWidth, maxVideoHeight,
          viewportWidth, viewportHeight, orientationMayChange);
      if (adaptiveTracks.length > 0) {
        return adaptiveVideoTrackSelectionFactory.createTrackSelection(group, adaptiveTracks);
      }
    }
    return null;
  }

  private static int[] getAdaptiveTracksForGroup(TrackGroup group, int[] formatSupport,
      boolean allowMixedMimeTypes, int requiredAdaptiveSupport, int maxVideoWidth,
      int maxVideoHeight, int viewportWidth, int viewportHeight, boolean orientationMayChange) {
    if (group.length < 2) {
      return NO_TRACKS;
    }

    List<Integer> selectedTrackIndices = getViewportFilteredTrackIndices(group, viewportWidth,
        viewportHeight, orientationMayChange);
    if (selectedTrackIndices.size() < 2) {
      return NO_TRACKS;
    }

    String selectedMimeType = null;
    if (!allowMixedMimeTypes) {
      // Select the mime type for which we have the most adaptive tracks.
      HashSet<String> seenMimeTypes = new HashSet<>();
      int selectedMimeTypeTrackCount = 0;
      for (int i = 0; i < selectedTrackIndices.size(); i++) {
        int trackIndex = selectedTrackIndices.get(i);
        String sampleMimeType = group.getFormat(trackIndex).sampleMimeType;
        if (!seenMimeTypes.contains(sampleMimeType)) {
          seenMimeTypes.add(sampleMimeType);
          int countForMimeType = getAdaptiveTrackCountForMimeType(group, formatSupport,
              requiredAdaptiveSupport, sampleMimeType, maxVideoWidth, maxVideoHeight,
              selectedTrackIndices);
          if (countForMimeType > selectedMimeTypeTrackCount) {
            selectedMimeType = sampleMimeType;
            selectedMimeTypeTrackCount = countForMimeType;
          }
        }
      }
    }

    // Filter by the selected mime type.
    filterAdaptiveTrackCountForMimeType(group, formatSupport, requiredAdaptiveSupport,
        selectedMimeType, maxVideoWidth, maxVideoHeight, selectedTrackIndices);

    return selectedTrackIndices.size() < 2 ? NO_TRACKS : Util.toArray(selectedTrackIndices);
  }

  private static int getAdaptiveTrackCountForMimeType(TrackGroup group, int[] formatSupport,
      int requiredAdaptiveSupport, String mimeType, int maxVideoWidth, int maxVideoHeight,
      List<Integer> selectedTrackIndices) {
    int adaptiveTrackCount = 0;
    for (int i = 0; i < selectedTrackIndices.size(); i++) {
      int trackIndex = selectedTrackIndices.get(i);
      if (isSupportedAdaptiveVideoTrack(group.getFormat(trackIndex), mimeType,
          formatSupport[trackIndex], requiredAdaptiveSupport, maxVideoWidth, maxVideoHeight)) {
        adaptiveTrackCount++;
      }
    }
    return adaptiveTrackCount;
  }

  private static void filterAdaptiveTrackCountForMimeType(TrackGroup group, int[] formatSupport,
      int requiredAdaptiveSupport, String mimeType, int maxVideoWidth, int maxVideoHeight,
      List<Integer> selectedTrackIndices) {
    for (int i = selectedTrackIndices.size() - 1; i >= 0; i--) {
      int trackIndex = selectedTrackIndices.get(i);
      if (!isSupportedAdaptiveVideoTrack(group.getFormat(trackIndex), mimeType,
          formatSupport[trackIndex], requiredAdaptiveSupport, maxVideoWidth, maxVideoHeight)) {
        selectedTrackIndices.remove(i);
      }
    }
  }

  private static boolean isSupportedAdaptiveVideoTrack(Format format, String mimeType,
      int formatSupport, int requiredAdaptiveSupport, int maxVideoWidth, int maxVideoHeight) {
    return isSupported(formatSupport) && ((formatSupport & requiredAdaptiveSupport) != 0)
        && (mimeType == null || Util.areEqual(format.sampleMimeType, mimeType))
        && (format.width == Format.NO_VALUE || format.width <= maxVideoWidth)
        && (format.height == Format.NO_VALUE || format.height <= maxVideoHeight);
  }

  /*
   *  Para forzar la pista de video elegimos un valor de "selectedGroup" y "selectedTrackIndex"
   *
   *  en la llamada a: new FixedTrackSelection(selectedGroup, selectedTrackIndex);
   *
   * */
  private TrackSelection selectFixedVideoTrack(TrackGroupArray groups,
                                               int[][] formatSupport, int maxVideoWidth, int maxVideoHeight, int viewportWidth,
                                               int viewportHeight, boolean orientationMayChange, boolean exceedConstraintsIfNecessary) {
    TrackGroup selectedGroup = null;

    int selectedTrackIndex = 0;
    int selectedPixelCount = Format.NO_VALUE;
    boolean selectedIsWithinConstraints = false;
    for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
      TrackGroup group = groups.get(groupIndex);
      List<Integer> selectedTrackIndices = getViewportFilteredTrackIndices(group, viewportWidth,
          viewportHeight, orientationMayChange);
      int[] trackFormatSupport = formatSupport[groupIndex];
      for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
        if (isSupported(trackFormatSupport[trackIndex])) {
          Format format = group.getFormat(trackIndex);
          boolean isWithinConstraints = selectedTrackIndices.contains(trackIndex)
              && (format.width == Format.NO_VALUE || format.width <= maxVideoWidth)
              && (format.height == Format.NO_VALUE || format.height <= maxVideoHeight);
          int pixelCount = format.getPixelCount();
          boolean selectTrack;
          if (selectedIsWithinConstraints) {
            selectTrack = isWithinConstraints
                && comparePixelCounts(pixelCount, selectedPixelCount) > 0;
          } else {
            selectTrack = isWithinConstraints || (exceedConstraintsIfNecessary
                && (selectedGroup == null
                || comparePixelCounts(pixelCount, selectedPixelCount) < 0));
          }
          if (selectTrack) {
            selectedGroup = group;
            selectedTrackIndex = trackIndex;
            selectedPixelCount = pixelCount;
            selectedIsWithinConstraints = isWithinConstraints;
          }
        }
      }
    }

    // si hemos especificado un trackIndex...
    if (this.selectedVideoTrackIndex != -1) {
      Log.d("[TrackSelector]", "selectedVideoTrackIndex=" + this.selectedVideoTrackIndex + "!!!!! yeah");
      selectedTrackIndex = this.selectedVideoTrackIndex; // este hay que cambiarlo por selectedTrackIndex
    }
    // si hemos especificado un groupIndex...
    if (this.selectedVideoGroupIndex != -1) {
      try {
        selectedGroup = groups.get(this.selectedVideoGroupIndex);
      } catch (Exception e) {
        Log.d("[TrackSelector]", "El selectedVideoGroupIndex excede los limites del array. Seleccionando indice de grupo=0...");
        selectedGroup = groups.get(0);
      }
    }
    // devolvemos la custom track selection o la selección por defecto
    Log.d("[TrackSelector]", "selectedGroup=" + selectedGroup + "!!!!! yeah");
    Log.d("[TrackSelector]", "selectedTrackIndex=" + selectedTrackIndex + "!!!!! yeah");
    return selectedGroup == null ? null
        : new FixedTrackSelection(selectedGroup, selectedTrackIndex);
  }

  /**
   * Compares two pixel counts for order. A known pixel count is considered greater than
   * {@link Format#NO_VALUE}.
   *
   * @param first The first pixel count.
   * @param second The second pixel count.
   * @return A negative integer if the first pixel count is less than the second. Zero if they are
   *     equal. A positive integer if the first pixel count is greater than the second.
   */
  private static int comparePixelCounts(int first, int second) {
    return first == Format.NO_VALUE ? (second == Format.NO_VALUE ? 0 : -1)
        : (second == Format.NO_VALUE ? 1 : (first - second));
  }


  // Audio track selection implementation.
  /*
  *  Para forzar la pista de audio elegimos un valor de "selectedGroup" y "selectedTrackIndex"
  *
  *  en la llamada a: new FixedTrackSelection(selectedGroup, selectedTrackIndex);
  *
  * */
  protected TrackSelection selectAudioTrack(TrackGroupArray groups, int[][] formatSupport,
      String preferredAudioLanguage) {
    TrackGroup selectedGroup = null;
    int selectedTrackIndex = 0;
    int selectedTrackScore = 0;
    for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
      TrackGroup trackGroup = groups.get(groupIndex);
      int[] trackFormatSupport = formatSupport[groupIndex];
      for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
        if (isSupported(trackFormatSupport[trackIndex])) {
          Format format = trackGroup.getFormat(trackIndex);
          boolean isDefault = (format.selectionFlags & Format.SELECTION_FLAG_DEFAULT) != 0;
          int trackScore;
          if (formatHasLanguage(format, preferredAudioLanguage)) {
            if (isDefault) {
              trackScore = 4;
            } else {
              trackScore = 3;
            }
          } else if (isDefault) {
            trackScore = 2;
          } else {
            trackScore = 1;
          }
          if (trackScore > selectedTrackScore) {
            selectedGroup = trackGroup;
            selectedTrackIndex = trackIndex;
            selectedTrackScore = trackScore;
          }
        }
      }
    }
    // si hemos especificado un trackIndex...
    if (this.selectedAudioTrackIndex != -1) {
      selectedTrackIndex = this.selectedAudioTrackIndex; // este hay que cambiarlo por selectedTrackIndex
    }
    // si hemos especificado un groupIndex...
    if (this.selectedAudioGroupIndex != -1) {
      try {
        selectedGroup = groups.get(this.selectedAudioGroupIndex);
      } catch (Exception e) {
        Log.d("[TrackSelector]", "El selectedAudioGroupIndex excede los limites del array. Seleccionando indice de grupo=0...");
        selectedGroup = groups.get(0);
      }
    }
    // devolvemos la custom track selection o la selección por defecto
    return selectedGroup == null ? null
        : new FixedTrackSelection(selectedGroup, selectedTrackIndex);
  }

  // Text track selection implementation.

  protected TrackSelection selectTextTrack(TrackGroupArray groups, int[][] formatSupport,
      String preferredTextLanguage, String preferredAudioLanguage) {
    TrackGroup selectedGroup = null;
    int selectedTrackIndex = 0;
    int selectedTrackScore = 0;
    for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
      TrackGroup trackGroup = groups.get(groupIndex);
      int[] trackFormatSupport = formatSupport[groupIndex];
      for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
        if (isSupported(trackFormatSupport[trackIndex])) {
          Format format = trackGroup.getFormat(trackIndex);
          boolean isDefault = (format.selectionFlags & Format.SELECTION_FLAG_DEFAULT) != 0;
          boolean isForced = (format.selectionFlags & Format.SELECTION_FLAG_FORCED) != 0;
          int trackScore;
          if (formatHasLanguage(format, preferredTextLanguage)) {
            if (isDefault) {
              trackScore = 6;
            } else if (!isForced) {
              // Prefer non-forced to forced if a preferred text language has been specified. Where
              // both are provided the non-forced track will usually contain the forced subtitles as
              // a subset.
              trackScore = 5;
            } else {
              trackScore = 4;
            }
          } else if (isDefault) {
            trackScore = 3;
          } else if (isForced) {
            if (formatHasLanguage(format, preferredAudioLanguage)) {
              trackScore = 2;
            } else {
              trackScore = 1;
            }
          } else {
            trackScore = 0;
          }
          if (trackScore > selectedTrackScore) {
            selectedGroup = trackGroup;
            selectedTrackIndex = trackIndex;
            selectedTrackScore = trackScore;
          }
        }
      }
    }
    Log.d("[TrackSelection]","DefaultFixedTrackSelector, selectedVideoTrackIndex=" + selectedTrackIndex);
    return selectedGroup == null ? null
        : new FixedTrackSelection(selectedGroup, selectedTrackIndex);
  }

  // General track selection methods.

  protected TrackSelection selectOtherTrack(int trackType, TrackGroupArray groups,
      int[][] formatSupport) {
    TrackGroup selectedGroup = null;
    int selectedTrackIndex = 0;
    int selectedTrackScore = 0;
    for (int groupIndex = 0; groupIndex < groups.length; groupIndex++) {
      TrackGroup trackGroup = groups.get(groupIndex);
      int[] trackFormatSupport = formatSupport[groupIndex];
      for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
        if (isSupported(trackFormatSupport[trackIndex])) {
          Format format = trackGroup.getFormat(trackIndex);
          boolean isDefault = (format.selectionFlags & Format.SELECTION_FLAG_DEFAULT) != 0;
          int trackScore = isDefault ? 2 : 1;
          if (trackScore > selectedTrackScore) {
            selectedGroup = trackGroup;
            selectedTrackIndex = trackIndex;
            selectedTrackScore = trackScore;
          }
        }
      }
    }
    return selectedGroup == null ? null
        : new FixedTrackSelection(selectedGroup, selectedVideoTrackIndex);
  }

  private static boolean isSupported(int formatSupport) {
    return (formatSupport & RendererCapabilities.FORMAT_SUPPORT_MASK)
        == RendererCapabilities.FORMAT_HANDLED;
  }

  private static boolean formatHasLanguage(Format format, String language) {
    return language != null && language.equals(Util.normalizeLanguageCode(format.language));
  }

  // Viewport size util methods.

  private static List<Integer> getViewportFilteredTrackIndices(TrackGroup group, int viewportWidth,
      int viewportHeight, boolean orientationMayChange) {
    // Initially include all indices.
    ArrayList<Integer> selectedTrackIndices = new ArrayList<>(group.length);
    for (int i = 0; i < group.length; i++) {
      selectedTrackIndices.add(i);
    }

    if (viewportWidth == Integer.MAX_VALUE || viewportHeight == Integer.MAX_VALUE) {
      // Viewport dimensions not set. Return the full set of indices.
      return selectedTrackIndices;
    }

    int maxVideoPixelsToRetain = Integer.MAX_VALUE;
    for (int i = 0; i < group.length; i++) {
      Format format = group.getFormat(i);
      // Keep track of the number of pixels of the selected format whose resolution is the
      // smallest to exceed the maximum size at which it can be displayed within the viewport.
      // We'll discard formats of higher resolution.
      if (format.width > 0 && format.height > 0) {
        Point maxVideoSizeInViewport = getMaxVideoSizeInViewport(orientationMayChange,
            viewportWidth, viewportHeight, format.width, format.height);
        int videoPixels = format.width * format.height;
        if (format.width >= (int) (maxVideoSizeInViewport.x * FRACTION_TO_CONSIDER_FULLSCREEN)
            && format.height >= (int) (maxVideoSizeInViewport.y * FRACTION_TO_CONSIDER_FULLSCREEN)
            && videoPixels < maxVideoPixelsToRetain) {
          maxVideoPixelsToRetain = videoPixels;
        }
      }
    }

    // Filter out formats that exceed maxVideoPixelsToRetain. These formats have an unnecessarily
    // high resolution given the size at which the video will be displayed within the viewport. Also
    // filter out formats with unknown dimensions, since we have some whose dimensions are known.
    if (maxVideoPixelsToRetain != Integer.MAX_VALUE) {
      for (int i = selectedTrackIndices.size() - 1; i >= 0; i--) {
        Format format = group.getFormat(selectedTrackIndices.get(i));
        int pixelCount = format.getPixelCount();
        if (pixelCount == Format.NO_VALUE || pixelCount > maxVideoPixelsToRetain) {
          selectedTrackIndices.remove(i);
        }
      }
    }

    return selectedTrackIndices;
  }

  /**
   * Given viewport dimensions and video dimensions, computes the maximum size of the video as it
   * will be rendered to fit inside of the viewport.
   */
  private static Point getMaxVideoSizeInViewport(boolean orientationMayChange, int viewportWidth,
      int viewportHeight, int videoWidth, int videoHeight) {
    if (orientationMayChange && (videoWidth > videoHeight) != (viewportWidth > viewportHeight)) {
      // Rotation is allowed, and the video will be larger in the rotated viewport.
      int tempViewportWidth = viewportWidth;
      viewportWidth = viewportHeight;
      viewportHeight = tempViewportWidth;
    }

    if (videoWidth * viewportHeight >= videoHeight * viewportWidth) {
      // Horizontal letter-boxing along top and bottom.
      return new Point(viewportWidth, Util.ceilDivide(viewportWidth * videoHeight, videoWidth));
    } else {
      // Vertical letter-boxing along edges.
      return new Point(Util.ceilDivide(viewportHeight * videoWidth, videoHeight), viewportHeight);
    }
  }

}
