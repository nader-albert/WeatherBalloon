package com.cammy.weather.consume

import scala.annotation.tailrec
import scala.util.parsing.combinator.lexical._


/**
 * Created by nader albert on 17/11/2015.
 */


case class Box(height: Int, width: Int)

object Interview extends App{

  def isWider(w: Int) = (b: Box) => (b.height * b.width) > w

  def isHigher(h: Int) = (b: Box) => b.height > h

  def and(first: Boolean, second: Boolean) = first && second

  isWider(10)(Box(10,100)) //returns false
  isHigher(5)(Box(10,10))  // returns true

  println (sum(0,10))

  @tailrec
  def sum(summation: Int, currentNumber :Int): Int ={
    currentNumber match {
      case x if currentNumber > 0 => sum(currentNumber + summation, currentNumber-1)
      case n => summation
    }
  }

  val chars = List('a', 'a', 'b', 'c', 'a', 'd') //List(('a', 2), ('b', 1'), ('c', 1), ('a', 1), ('d', 1))
  val keyValueList: List[(Char, Int)] = chars.flatMap(char => List((char,1)))

  //val keyValueList = chars.map(char => (char,1))(collection.breakOut)
  //val accumulative: List[(Char, Int)] = List.empty[(Char,Int)]

  println (func(keyValueList, Nil) )

  val z = for (
    char <- chars;
    keyValue <- keyValueList
    if keyValue._1 == char
  ) yield List(char,1)

  //val z = keyValueList.fold(Map.empty[(Char,Int), Int]) ((acc, current) => (acc._1,acc._2 + 1 ))

  //keyValueList
    /*accumulativeList match  {
      case Nil => accumulativeList.(current)

      case head::tail =>
    } )*/

  def func(inputList: List[(Char,Int)], accumulativeList: List[(Char,Int)]): List[(Char,Int)] = {
    inputList match {
      case Nil => accumulativeList
      case head::Nil =>
        if (accumulativeList.last._1 == head._1)
          accumulativeList.reverse.tail.::(accumulativeList.last._1, accumulativeList.last._2 +1)
        else
          accumulativeList.::(head)
      case head::tail =>
        if (head._1 == tail.head._1)
          func(tail.tail, accumulativeList.::(head._1, head._2 + 1))
        else
          func(tail, accumulativeList.::(head._1, head._2))
    }
  }
}
