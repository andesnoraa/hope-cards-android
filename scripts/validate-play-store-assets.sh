#!/bin/sh
set -eu

store_root="assets/store"
expected_locales="en-US fil-PH ml-IN de-DE fr-FR it-IT es-419"

for locale in $expected_locales; do
  listing="$store_root/listings/$locale"
  title_length=$(tr -d '\n' < "$listing/title.txt" | wc -m | tr -d ' ')
  short_length=$(tr -d '\n' < "$listing/short-description.txt" | wc -m | tr -d ' ')
  full_length=$(wc -m < "$listing/full-description.txt" | tr -d ' ')

  [ "$title_length" -le 30 ] || { echo "$locale title exceeds 30 characters" >&2; exit 1; }
  [ "$short_length" -le 80 ] || { echo "$locale short description exceeds 80 characters" >&2; exit 1; }
  [ "$full_length" -le 4000 ] || { echo "$locale full description exceeds 4000 characters" >&2; exit 1; }
  [ "$(grep -cve '^[[:space:]]*$' "$listing/screenshot-captions.txt")" -eq 6 ] || { echo "$locale must have six screenshot captions" >&2; exit 1; }
  [ "$(grep -cve '^[[:space:]]*$' "$listing/screenshot-alt-text.txt")" -eq 6 ] || { echo "$locale must have six screenshot alt text entries" >&2; exit 1; }
  [ -s "$listing/video-title.txt" ] || { echo "$locale must have a promo video title" >&2; exit 1; }
  [ -s "$listing/video-description.txt" ] || { echo "$locale must have a promo video description" >&2; exit 1; }

  phone_count=$(find "$store_root/screenshots/phone/$locale" -type f -name '*.png' | wc -l | tr -d ' ')
  tablet_count=$(find "$store_root/screenshots/tablet/$locale" -type f -name '*.png' | wc -l | tr -d ' ')
  [ "$phone_count" -eq 6 ] || { echo "$locale must have six phone screenshots" >&2; exit 1; }
  [ "$tablet_count" -eq 2 ] || { echo "$locale must have two tablet screenshots" >&2; exit 1; }

  for image in "$store_root/screenshots/phone/$locale"/*.png; do
    dimensions=$(sips -g pixelWidth -g pixelHeight "$image" 2>/dev/null)
    printf '%s' "$dimensions" | grep -q 'pixelWidth: 1080' || { echo "$image has the wrong width" >&2; exit 1; }
    printf '%s' "$dimensions" | grep -q 'pixelHeight: 1920' || { echo "$image has the wrong height" >&2; exit 1; }
  done

  for image in "$store_root/screenshots/tablet/$locale"/*.png; do
    dimensions=$(sips -g pixelWidth -g pixelHeight "$image" 2>/dev/null)
    printf '%s' "$dimensions" | grep -q 'pixelWidth: 1600' || { echo "$image has the wrong width" >&2; exit 1; }
    printf '%s' "$dimensions" | grep -q 'pixelHeight: 2560' || { echo "$image has the wrong height" >&2; exit 1; }
  done

  feature="$store_root/feature-graphics/$locale/feature-graphic.png"
  dimensions=$(sips -g pixelWidth -g pixelHeight "$feature" 2>/dev/null)
  printf '%s' "$dimensions" | grep -q 'pixelWidth: 1024' || { echo "$feature has the wrong width" >&2; exit 1; }
  printf '%s' "$dimensions" | grep -q 'pixelHeight: 500' || { echo "$feature has the wrong height" >&2; exit 1; }

  video="$store_root/videos/$locale/hope-cards-promo-$locale.mp4"
  if [ -f "$video" ]; then
    video_width=$(ffprobe -v error -select_streams v:0 -show_entries stream=width -of csv=p=0 "$video")
    video_height=$(ffprobe -v error -select_streams v:0 -show_entries stream=height -of csv=p=0 "$video")
    video_duration=$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$video" | cut -d. -f1)
    [ "$video_width" -eq 1920 ] || { echo "$video has the wrong width" >&2; exit 1; }
    [ "$video_height" -eq 1080 ] || { echo "$video has the wrong height" >&2; exit 1; }
    [ "$video_duration" -ge 20 ] && [ "$video_duration" -le 60 ] || { echo "$video duration must be between 20 and 60 seconds" >&2; exit 1; }
  fi
done

icon_dimensions=$(sips -g pixelWidth -g pixelHeight "$store_root/play-icon-512.png" 2>/dev/null)
printf '%s' "$icon_dimensions" | grep -q 'pixelWidth: 512' || { echo "Play icon has the wrong width" >&2; exit 1; }
printf '%s' "$icon_dimensions" | grep -q 'pixelHeight: 512' || { echo "Play icon has the wrong height" >&2; exit 1; }

default_feature_dimensions=$(sips -g pixelWidth -g pixelHeight "$store_root/feature-graphic.png" 2>/dev/null)
printf '%s' "$default_feature_dimensions" | grep -q 'pixelWidth: 1024' || { echo "Default feature graphic has the wrong width" >&2; exit 1; }
printf '%s' "$default_feature_dimensions" | grep -q 'pixelHeight: 500' || { echo "Default feature graphic has the wrong height" >&2; exit 1; }

echo "Play Store assets validated for 7 locales."
