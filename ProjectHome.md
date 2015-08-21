In this project we implement an Android application for cc98.org, a student forum in Zhejiang University, China. cc98 is quite popular among students in Zhejiang University, where they could share diverse information with each other on campus, including courses and homework, travel and food, career plan and job hunting, news and events, etc. However, cc98 does not have an official Android version, which makes it unpleasant to browse cc98 on mobile devices.

In this project, we want to simulate Http POST/GET requests of cc98, and parse HTML from this site to present contents to users(Since we do not have access to the data API, we could only wrap this site in this manner). We utilize HttpWatch to analyze POST/GET request data, and use Jsoup for HTML parsing. We are trying hard to design an elegant UI to facilitate user experience of this app.