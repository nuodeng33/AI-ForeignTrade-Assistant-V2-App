# V3 Implementation Roadmap

## Completed Foundation In This Branch

- Added dev/test/prod build flavors and centralized API base URL in `BuildConfig`.
- Added a shared OkHttp/Retrofit client with token injection and environment-aware logging.
- Added secure session management with legacy SharedPreferences compatibility.
- Added Repository and ViewModel layers for authentication, orders, profile, and chat.
- Moved login, register, order list, create order, and chat streaming flows toward MVVM.
- Hardened WebView navigation with HTTPS-only trusted host validation.
- Added Android CI workflow for build, unit test, and lint.
- Added API contract documentation for backend alignment.

## Next Milestones

1. Replace fixed order cards with a full RecyclerView order list.
2. Add Room or DataStore cache for orders, profile, and chat history.
3. Replace personal-center mock statistics with real backend endpoints.
4. Add real logistics API integration and a dedicated logistics timeline model.
5. Replace deprecated image picker and storage permission flow with Activity Result APIs.
6. Add unit tests around repositories, ViewModels, and streaming response parsing.
7. Replace production API placeholder with the real HTTPS domain before release.
