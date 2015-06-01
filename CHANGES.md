#Version 0.5.1

## Changes

- Added configurable preferred upload service per extension
- Added drop shadow to images
- Added saving of window position
- Fixed possible memory leaks

# Version 0.5

## Changes

- Images can be removed from a library
- Images in view can be resized (option in view menu)
- Added shadow around images to improve visual separation
- Clicking on the upload/copy notifications visits the URL
- Search clear button on right of component
- Search components are disabled when no library exists
- Added support for system drag modifier keys (ex. In Windows, hold ctrl to copy rather than move)
- Added option to monitor library folders for new images (enabled by default)
- Added option to use system window decoration
- Moved "Create from folder" functionality into "Create new". Checks of supported images exist in the selected folder.
- Added messages in main image panel to notify user to create a library or add images

## Fixes

- Image menu information is updated immediately rather than when reopened (tags, upload)
- Reworked how image animations are triggered to hopefully fix a number of bugs

## Known Issues

- When resizing the window or changing image size, image thumbnails may not load until the image area is scrolled