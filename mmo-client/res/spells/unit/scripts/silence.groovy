import core.board.Board
import core.board.Cell
import core.board.Spell
import core.board.Unit
import core.graphics.scenes.BattleScene
import core.graphics.scenes.Scenes
import program.main.Util

def onCheck(Spell spell, Board board, Cell target){
    Util.distance(spell.caster.position, target) <= 5
}

def onCheckAOE(Spell spell, Board board, Cell from, Cell to){
    Util.distance(from, to) <= 3
}

def onCastBegin(Spell spell, Board board, Cell target){
	spell.caster.setFacing(target);
	1.0F
}

def onCastEnd(Spell spell, Board board, Cell target){
    def units = board.units.findAll { it != spell.caster && Util.distance(it.position, target) <= 3 }

    units.each {
        def to = Util.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(it).node;
        board.addEffect("heal-effect", to)
    }

    board.addBuff("silenceBuff", 1, 6, units)
	board.nextTurn()
	spell.putOnCoolDown()
}