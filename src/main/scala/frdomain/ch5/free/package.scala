package frdomain.ch5

import cats.free.Free

package object free {
  type AccountRepo[A] = Free[AccountRepoF, A]
}

