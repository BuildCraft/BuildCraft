Subprojects are for code that is used by buildcraft, but doesn't depend on buildcraft (or for some of them, minecraft itself). This is provided in the (vague) hope that these might be useful for others.

If this is too much of a maintenance burden then it should always be possible to merge the library back in with the rest of the source.

Generally all subprojects should be committed to separately so that "git subtree" can be used to manage them separately.

Currently we only have 1 subproject: the expression library. We *could* store the API in this folder, but that would be a breaking change for existing forks.
