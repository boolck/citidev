<!-- Add banner here -->

# Project Title
CitiBank Quant Dev Java coding exercise : Order Book Engine

# Table of contents

- [Project Title](#project-title)
- [Table of contents](#table-of-contents)
- [Description](#Description)
- [Installation](#installation)
- [Usage](#usage)
- [Design](#design)
- [Enhancement](#enhancement)
- [Release History](#release-history)

# Description
[(Back to top)](#table-of-contents)

This is a simple Order book processing engine that works on following principles:
- Orders -
    - An order is instruction from client to purchase or sell a security. 
    - It is made of timestamp, orderid, buy/sell, price, quantity & a unique sequence number.
    - Each order event represents either Add, Update or Cancel operation.
- BBO (Best Bid Offer)
    - Level 1 market data representing highest Bid and lowest ask as of that time.
    - It is made of timestamp, bidprice, bidqty, askprice, askqty and unique sequence number.
- Order Book Engine 
    - Implements functional requirement of processing orders .
    - Checks if order qty and price are above error thresholds then create BBO as of that time.
    - See Design section for more details.

# Installation
[(Back to top)](#table-of-contents)

To use this project, first clone the repo on your device using the command below:

```git init```

```git clone https://github.com/boolck/citidev.git``` 

Verify that repository is checked out & then run

```mvn clean package```

# Usage
[(Back to top)](#table-of-contents)

- A main class Application is created to take in input of order requests from file. 
- If no file is provided, default input_orders.csv is used.

To call the main ApplicationRunner class:

```mvn clean compile exec:java```

or explicitly call it by passing argument

```mvn clean compile exec:java -Dexec.mainClass="com.gh.dev.ApplicationRunner"```

The test setup that uses provided  & 11 csv can be run by 

```mvn clean test```

# Design
[(Back to top)](#table-of-contents)

There are 2 main actors of the program
- Input Request parser - 
    - BufferedCSVListener : used when input source of orders is persisted format in  csv
-  Order booking engine  
    - main app having all the functional processing logic to process requests from  csv listener
    - it maintains 2 priority queues - one for bid and other for ask and keeps updating them as new request comes
    
# Enhancement
[(Back to top)](#table-of-contents)

Enhancements for future releases:
- Support multiple instruments in order book
- Setup APIs to support synchronous input order request
- Implement batching for parsing & processing input order requests

# Release History
[(Back to top)](#table-of-contents)

Release ver   | Description
------------- | -------------
1.0-SNAPSHOT  | First release. Supports Order add, cancel and update & creates Best Bid Offer.

