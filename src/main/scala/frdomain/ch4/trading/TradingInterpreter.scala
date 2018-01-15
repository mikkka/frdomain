package frdomain.ch4
package trading

import cats.data._
import TradeModel._

trait TradingInterpreter extends Trading[Account, Trade, ClientOrder, Order, Execution, Market] {

  def clientOrders: Kleisli[List, List[ClientOrder], Order] = Kleisli(fromClientOrders)

  def execute(market: Market, brokerAccount: Account) = Kleisli[List, Order, Execution] { order =>
    order.items.map { item =>
      Execution(brokerAccount, item.ins, "e-123", market, item.price, item.qty)
    }
  }

  def allocate(accounts: List[Account]) = Kleisli[List, Execution, Trade] { execution =>
    val q = execution.quantity / accounts.size
    accounts.map { account =>
      makeTrade(account, execution.instrument, "t-123", execution.market, execution.unitPrice, q)
    }
  }
}

object TradingInterpreter extends TradingInterpreter

