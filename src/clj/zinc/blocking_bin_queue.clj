(ns zinc.blocking-bin-queue)

(gen-class
  :name zinc.BlockingBinQueue
  :implements java.concurrent.BlockingQueue
  :state state
  :prefix "-"
  :main false
)