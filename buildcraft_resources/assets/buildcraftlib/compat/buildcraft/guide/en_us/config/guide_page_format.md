Guide pages are all written in a subset of markdown.

<italic>(Most of this page needs filling out, however here is a list of basic tags)</italic>

# Simple single tags
Every tag has is in the form &lt;name arguments/&gt; - where the name is the tag name, and the ending '/' signifies that this tag is by itself. Some tags also have arguments in the form of "key-value pairs", for example:
<guide_md>
"level"="42"
</guide_md>

## New Page
&lt;new_page/&gt; will move the content after it to the next page.

## Chapters
&lt;chapter/&gt; will create a "chapter heading", that's displayed in the chapter list as a coloured heading. It has one possible argument: "level", which must be an integer that's 0 or greater. Alternatively you can use multiple hash symbols (#) just like normal markdown format to create headings. The number of #'s (minus one) equals the level of the heading.
For example:
<guide_md>
 ### Level 3 chapter
&lt;chapter name="Another level 3 chapter" level="2"/&gt;
</guide_md>
will result in:
### Level 3 chapter
<chapter name="Another level 3 chapter" level="2"/>

# Simple double tags
All of these must surround some content: and they must have both a starting tag &lt;italic&gt; and an ending tag &lt;/italic&gt;
They all affect the contained text between the starting and end tags.

- <italic>italic</italic>
- <bold>bold</bold>
- <underline>underlined</underline>
- <strikethrough>strikethrough</strikethrough>
- <red>red</red>
- <black>black</black>
- <dark_blue>dark_blue</dark_blue>
- <dark_green>dark_green</dark_green>
- <dark_aqua>dark_aqua</dark_aqua>
- <dark_red>dark_red</dark_red>
- <dark_purple>dark_purple</dark_purple>
- <gold>gold</gold>
- <gray>gray</gray>
- <dark_gray>dark_gray</dark_gray>
- <blue>blue</blue>
- <green>green</green> (green)
- <aqua>aqua</aqua> (aqua)
- <light_purple>light_purple</light_purple>
- <yellow>yellow</yellow> (yellow)
- <white>white</white> (white)

It is not a good idea to use green, aqua, yellow, or white because it is generally very difficult to see any of those colours on the page.

# Toggled tags
All of the following tags are option based, and can be enabled or disabled by the player.

## Lore
If the "Show Lore [ ]" box is ticked then everything in the &lt;no_lore&gt; tag will be hidden, otherwise everything in the &lt;lore&gt; tag will be hidden.

The lore tag should be used to show "in-game" guide information, narrated from the perspective of the player. (As if they were actively researching how everything worked). The inverse is for a more formal, wikipedia-like tone used to convey the information, pure and simple.

This is enabled by default in-game, and always disabled in exports.

## Hints
If the "Show Hints [ ]" box is ticked then everything in the &lt;no_hint&gt; tag will be hidden, otherwise everything in the &lt;hint&gt; tag will be hidden.

The hint tag should be used to show extra "hints" that the described thing can be used for, or different ways of using it in combinations with other blocks/items. Essentially this is for "recommended usages" of different things. (As working this out is part of the fun it is not required. As such this is disabled by default, but toggle-able in-game and planned to be toggle-able when exported.

## Detail
If the "display.guideBookEnableDetail" configuration option is set to "true" then everything in the &lt;no_detail&gt; tag will be hidden, otherwise everything in the &lt;detail&gt; tag will be hidden.

The detail tag should be used to show all of the numbers used when calculating various things, like pipe flow rate, extraction rate, pulse rate, etc.

This is disabled by default, but toggle-able in-game and planned to be toggle-able when exported.

# Misc
## Recipe (singular)
This tag shows the first recipe found for the given item stack. For example:
<guide_md>
&lt;recipes stack="buildcraftcore:gear_stone"&gt;
</guide_md>
<recipe stack="buildcraftcore:gear_stone"/>

## Recipes
This tag shows all recipes for the given item stack. For example:
<guide_md>
&lt;recipes stack="buildcraftcore:gear_wood"&gt;
</guide_md>
<recipes stack="buildcraftcore:gear_wood" chapter_level="2"/>

## Usages
Similar to recipes, this shows all usages for the given item stack. For example:
<guide_md>
&lt;usages stack="buildcraftcore:engine"&gt;
</guide_md>
<usages stack="buildcraftcore:engine" chapter_level="2"/>

## Recipes and usages
This effectively combines the above two tags into one, for example:
<guide_md>
&lt;recipes_usages stack="buildcrafttransport:pipe_structure"&gt;
</guide_md>
<recipes_usages stack="buildcrafttransport:pipe_structure" chapter_level="2"/>

## Group
This shows groups appropriate for the given item, statement, etc.

## Images
This shows an image. There are 3 arguments:
* src: The source location for the image.
* width: The width that the image will be shown with. Defaults to the width of the source image (however this might not be what you want)
* height: The height that the image will be shown with.

For example:
<guide_md>
&lt;image src="buildcraftcore:items/wrench" width="64" height="64"/&gt;
</guide_md>
<image src="buildcraftcore:items/wrench" width="64" height="64"/>

