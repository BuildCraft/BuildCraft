# BC MJ power network usage

## Basic ideas
MJ is referred throughout as "watts" as the transmitted unit is never used directly- you say that you will provide X watts for as long as possible. Because a normal usage value is "1 Watt" we use milliWatts instead, which is 1/1000 of a watt.

MJ is not storable. You should convert it to EU before storing it (say for robots or tools). The conversion should be lossy, and the base conversion rate is 1 Watt in 1 tick is equivilent to *TODO* EU. You are encouraged to convert to and from at no higher than 50% efficiency (BC itself converts to at 20% efficiency).

It is fine however to store *small* amounts of MJ inside of machines if they are doing tasks that take longer than a single tick. You should not be able to store it and extract it later though, and you should empty the buffer after all incoming connections are broken.

## Power types

Redstone:
  - Used for simple tasks such as extracting an item from a chest or fluid from a tank.
  - Generally should amounts up to 100 milli watts of power
  - Should only be transferable over small contained distances (across 1 block)

Normal (*TODO Think Of Name*):
  - Used for big complex tasks (like quarrying or building)
  - Generally should be amounts between 1 and 40 watts of power
  - Should be transferable over medium distances (up to about 100 blocks)

Laser:
  - Used for small, complex tasks (like making an iron chipset or programming a robot board)
  - Generally should be amounts between 5 and 100 watts of power
  - Should be transferable over short distances (up to ~6 blocks)
    - The exception is if you are making a huge laser transfer which should transfer over long distances (say 10 to 20 km)
      - If you make a laser transferer make sure that it is cool

## Machine types

Producer:
  - Can create power from an external source- perhaps coal, liquid fuel or wind
  - You are asked 

Consumer:
  - Requests power from the network

## API usage
Machines should provide an instance of IMjMachine via a capability. The returned class *must* be an instance of either "IMjMachineProducer", "IMjMachineTransporter", "IMjMachineConverter" or "IMjMachineConsumer".

### Base interface (IMjMachine)
