class OInt(n: Int) extends Ordered[OInt] {
  var value = n

  def compare(that: OInt) = {
    var result = 0

    var diff = this.value - that.value
    if (diff != 0)
      result = (this.value - that.value) / Math.abs(this.value - that.value)

    result
  }
  override def toString() = (s"$value")
}

abstract class OTree[T <: Ordered[T]] extends Ordered[OTree[T]]

case class OLeaf[T <: Ordered[T]](t: T) extends OTree[T] {
  def compare(that: OTree[T]) = that match {
    case OLeaf(tt) => t.compare(tt) 
    case ONode(tl) => -1 
  }
}

case class ONode[T <: Ordered[T]](l: List[OTree[T]]) extends OTree[T] {
  def compare(that: OTree[T]) = that match {
    case ONode(tl) => 
      var result = 0
      if (l.nonEmpty && tl.nonEmpty) {
        result = l.head.compare(tl.head)
        
        if (result == 0)
          result = ONode(l.tail).compare(ONode(tl.tail))
      }

      if (result == 0) {
        val lLength = l.length
        val tlLength = tl.length
      
        if (lLength < tlLength)
          result = -1
        else if (lLength > tlLength)
          result = 1
      }
      
      result
      
    case OLeaf(tt) => 1
  }
}

object Part2 {
  def compareTrees[T <: Ordered[T]](t1: OTree[T], t2: OTree[T]): Unit = {
    var result = t1.compare(t2)

    if (result == -1)
      print("Less\n")
    else if (result == 0)
      print("Equal\n")
    else
      print("Greater\n")
  }

  def test(): Unit = {

    val tree1 = ONode(List(OLeaf(new OInt(6))))

    val tree2 = ONode(List(OLeaf(new OInt(3)),
			   OLeaf(new OInt(4)), 
			   ONode(List(OLeaf(new OInt(5)))), 
			   ONode(List(OLeaf(new OInt(6)), 
				      OLeaf(new OInt(7))))));

    val treeTree1: OTree[OTree[OInt]] = 
      ONode(List(OLeaf(OLeaf(new OInt(1)))))

    val treeTree2: OTree[OTree[OInt]] = 
      ONode(List(OLeaf(OLeaf(new OInt(1))),
		 OLeaf(ONode(List(OLeaf(new OInt(2)), 
				  OLeaf(new OInt(2)))))))

    print("tree1: ")
    println(tree1)
    print("tree2: ")
    println(tree2)
    print("treeTree1: ")
    println(treeTree1)
    print("treeTree2: ")
    println(treeTree2)
    print("Comparing tree1 and tree2: ")
    compareTrees(tree1, tree2)
    print("Comparing tree2 and tree2: ")
    compareTrees(tree2, tree2)
    print("Comparing tree2 and tree1: ")
    compareTrees(tree2, tree1)
    print("Comparing treeTree1 and treeTree2: ")
    compareTrees(treeTree1, treeTree2)
    print("Comparing treeTree2 and treeTree2: ")
    compareTrees(treeTree2, treeTree2)
    print("Comparing treeTree2 and treeTree1: ")
    compareTrees(treeTree2, treeTree1)
  }

  def main(args: Array[String]) = {
    test()
  }
}
