# Mobile-Platform-Development
## RSS Feed Parser (British Geological Survey)

### Introduction

The British Geological Survey records information about Earthquakes across the British Isles and beyond. (http://www.earthquakes.bgs.ac.uk/index.html ) This information is available as an XML feed and provides information about earthquakes that have occurred over the last 100 days.

The task is to “parse” the XML feed, store the data in an appropriate data structure and to provide some summary statistics in an aesthetically pleasing manner for the user of the application which will run on an Android Mobile Device.

### Specifications
- A list that the user can scroll through. The list should simply display the location and strength
- Colour coding that displays the earthquakes from strongest to weakest
- A means of displaying more detailed information on a specific earthquake from the displayed list
- Functionality which allows the user to enter a specific date and date range. When this facility is executed the user should be presented with the following information for the day or period entered:

  - Most Northerly/Southerly/Westerly/Easterly earthquake.
  - Largest magnitude Earthquake.
  - Deepest and shallowest earthquake.
  - The information that is being processed should update on a regular basis.
  - Portrait and landscape layouts that make appropriate use of the space available in that layout
  - A map view which allows the user to zoom in/out to view the location of a specific earthquake. The pins should be coloured code as per the strength of the earthquake.



## License

MIT License

Copyright (c) 2021 Madani Napaul

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
