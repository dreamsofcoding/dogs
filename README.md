# DOGS APP


An Android application demonstrating dog breeds and images using the [Dog CEO API](https://dog.ceo/dog-api/). 
Built in Kotlin with Jetpack Compose, MVVM architecture, Hilt for dependency injection, Retrofit for networking, 
Room for local persistence, and Coil for image loading.

## PROJECT SPECIFICATION
* The first screen should request a list of dog breeds from the Dogs API (https://dog.ceo/dog-api/) and present the result in a scrolling list.
* Tapping a breed from the first list should present the second screen.
* The second screen should show 10 random dog images of the selected breed.

### TECH STACK
* Kotlin
* Jetpack Compose for the UI
* Hilt for DI
* Coroutines and Flow
* Retrofit - for networking
* Room -for local caching
* mockk - for unit testing
* Coil - Image Loader library.

### PROJECT FEATURES

- **Splash & Navigation**
- **Breed List**
    - Displays all breeds in a lazy staggered grid with section headers (A, B, C…).
    - Alphabet sidebar for quick jumps.
    - Search bar to filter breeds by name or sub-breed.
    - Asynchronously loads a thumbnail for each breed via Coil.
- **Images Screen**
    - Hero banner at top (preview of selected image).
    - Staggered grid of up to 10 images per breed.
    - Refresh icon.
- **Offline Caching**
    - Persists breed list and downloaded images in a Room database.
    - Background download of images for offline viewing.