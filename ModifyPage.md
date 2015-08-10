---
layout: page
---

Besides adding content, you may want to alter different aspects of the page to fit to your project.

You can easily modify:

 - the navigation bar
 - the default look of the frontpage
 - the sidebar
 - the footer

The following settings should be updated for every project:

| Setting | Description |
| `humans.txt` | Actually I think there whould be a default pointer to CONTRIBUTORS.MD here? |
| `assets/img/logo.png` | Logo for your project. Generate the other files using some favicon generator service. Then fix the favicon names in `_config.yml`
| `_config.yml : title` | Project name | 
| `_config.yml : slogan` | Short slogan |
| `_config.yml : description` | Short project description |
| `_config.yml : url` | URL to your project without protocol, e.g. `//dkpro.github.io/dkpro-core` - make sure to leave `baseurl` empty! |
| `_config.yml : improve_content` | https URL for editing gh-pages branch |
| `_config.yml : urlimg` | Image URL in the form `/dkpro-core/images/` |
| `_data/socialmedia.yml | Social media links for your project (or comment out example links) |
| `_data/networm.yml | Mailing list links for your project (or comment out example links) |
| `_config.yml : google_analytics_tracking_id` | Enable Google Analytics |
| `_data/releases.yml | Your project releases |

The following files should be deleted

 * all files in `_drafts`
 * all files in `_posts`
 * all files in `images`
 * `README.md`
 * `ModifyPage.md`


Configuring the _config.yml
---------------------------

In the sites' root directory, edit `_config.yml` - the most important settings are under the first caption, `Site Settings`.

In particular, the following settings must be made:

  * set the `url` without the protocol, e.g. `url: "//dkpro.github.io/dkpro-core"` to avoid mixed http/https content when the site is accessed through https.
  * leave the `baseurl` empty

Running the page with a local Jekyll
------------------------------------

Edit the _config_dev.yml file:

    url: "http://localhost:4000"
    urlimg: "http://localhost:4000/images/"

and run Jekyll from the directory containing the cloned website using

    jekyll serve --config _config.yml,_config_dev.yml

Adding content pages
--------------------
In the folder `pages`, copy the file `example.md` and rename it.
Have a look at it, and modify it according to your needs. Every such file has to start with **front matter**,
i.e. two lines containing only `---`, between those go settings for the page (title, layout, etc.).
After that, just write your content in [Markdown syntax][1].

You find examples for more elaborate markdown in the `_drafts` folder, e.g. for including a TOC or a gallery.

You now may want to change the navigation, to make your new page accessible.


Customizing the navgiation bar
------------------------------
Open `_data/navigation.yml` and adapt it to your needs - it should be mostly self-explanatory.
A top level entry looks like this:

	- title: Downloads
	  url: "/downloads/"
	  side: left

You can also add a dropdown attribute.


Changing frontpage look
-----------------------
The content for your frontpage goes into the `index.md` in the root directory.
If you want the frontpage to hold special elements (like the icon table for the DKPro Core page),
you need to edit `_layouts/frontpage.html`.

Otherwise you can just change the `layout` of the frontpage inside `index.md`, e.g. to `page` or `page-fullwidth`.
Enabling the sidebar is as easy as adding an attribute `sidebar: right` (should be used with `layout: page`) to your frontmatter.


Sidebar content
---------------
To change the sidebar content (widgets etc.), edit `_includes/sidebar.html`.


Changing the footer
-------------------
Changing the footer involves editing the following files: `_data/services.yml` and `_data/network.yml`.

You might also want to change the `description` field in `_config.yml`, the content of which is shown in the footer on the left side.
If you want to change the "More" link which links to an info site, open `_includes/footer.html` and
find the line `<a href="{{ site.url }}/info/">{{ site.data.language.more }}</a>`; either change the link (e.g. removing `info/`, which links to your start page) or remove it altogether. Otherwise edit `pages/info.md`.

You can remove and add social media buttons to the subfooter using the file `_data/socialmedia.yml`.



[1]: http://daringfireball.net/projects/markdown/syntax
